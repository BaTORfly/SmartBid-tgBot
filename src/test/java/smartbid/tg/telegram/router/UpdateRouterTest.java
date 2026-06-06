package smartbid.tg.telegram.router;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import smartbid.tg.backend.BackendLot;
import smartbid.tg.backend.BackendLotClient;
import smartbid.tg.backend.LotSubmission;
import smartbid.tg.conversation.ConversationStep;
import smartbid.tg.conversation.InMemoryConversationStorage;
import smartbid.tg.telegram.callback.CallbackData;
import smartbid.tg.telegram.file.TelegramFileDownloader;
import smartbid.tg.telegram.handler.callback.OfferLotCallbackHandler;
import smartbid.tg.telegram.handler.command.StartCommandHandler;
import smartbid.tg.telegram.handler.message.LotDraftMessageHandler;
import smartbid.tg.telegram.keyboard.StartKeyboardFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateRouterTest {

    private final InMemoryConversationStorage conversationStorage = new InMemoryConversationStorage();
    private final FakeTelegramFileDownloader fileDownloader = new FakeTelegramFileDownloader();
    private final FakeBackendLotClient backendLotClient = new FakeBackendLotClient();
    private final UpdateRouter updateRouter = new UpdateRouter(
            List.of(new StartCommandHandler(new StartKeyboardFactory(), conversationStorage)),
            List.of(new OfferLotCallbackHandler(conversationStorage)),
            List.of(new LotDraftMessageHandler(conversationStorage, fileDownloader, backendLotClient))
    );

    @Test
    void handlesStartCommand() {
        Update update = new Update();
        Message message = messageWithText(123L, 77L, "/start");
        update.setMessage(message);

        List<BotApiMethod<?>> responses = updateRouter.route(update);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isInstanceOf(SendMessage.class);

        SendMessage response = (SendMessage) responses.get(0);
        assertThat(response.getChatId()).isEqualTo("123");
        assertThat(response.getText()).contains("SmartBid");
        assertThat(response.getReplyMarkup()).isNotNull();
    }

    @Test
    void ignoresUnknownTextWithoutActiveConversation() {
        Update update = new Update();
        update.setMessage(messageWithText(123L, 77L, "hello"));

        List<BotApiMethod<?>> responses = updateRouter.route(update);

        assertThat(responses).isEmpty();
    }

    @Test
    void startsLotDraftConversationFromOfferLotCallback() {
        Update update = new Update();
        CallbackQuery callbackQuery = callbackQuery(123L, 77L, CallbackData.OFFER_LOT);
        update.setCallbackQuery(callbackQuery);

        List<BotApiMethod<?>> responses = updateRouter.route(update);

        assertThat(responses).hasSize(1);
        SendMessage response = (SendMessage) responses.get(0);
        assertThat(response.getText()).contains("Напиши название лота");

        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> assertThat(state.step()).isEqualTo(ConversationStep.WAITING_TITLE));
    }

    @Test
    void collectsLotDraftAndSendsItToBackend() {
        route(callbackUpdate(123L, 77L, CallbackData.OFFER_LOT));

        SendMessage titleResponse = routeSingle(messageUpdate(messageWithText(123L, 77L, "iPhone 15")));
        assertThat(titleResponse.getText()).contains("описание");
        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> {
                    assertThat(state.step()).isEqualTo(ConversationStep.WAITING_DESCRIPTION);
                    assertThat(state.draft().title()).isEqualTo("iPhone 15");
                });

        SendMessage descriptionResponse = routeSingle(messageUpdate(messageWithText(123L, 77L, "256 GB, почти новый")));
        assertThat(descriptionResponse.getText()).contains("фото");
        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> {
                    assertThat(state.step()).isEqualTo(ConversationStep.WAITING_PHOTO);
                    assertThat(state.draft().description()).isEqualTo("256 GB, почти новый");
                });

        SendMessage photoResponse = routeSingle(messageUpdate(messageWithPhoto(123L, 77L, 456, "photo-file-id")));
        assertThat(photoResponse.getText()).contains("Лот отправлен на оценку");
        assertThat(photoResponse.getText()).contains("ID лота: ad-1");
        assertThat(photoResponse.getText()).contains("Начальная цена: 1.00 р");
        assertThat(conversationStorage.findByUserId(77L)).isEmpty();

        assertThat(fileDownloader.downloadedFileId).isEqualTo("photo-file-id");
        assertThat(backendLotClient.submission.draft().title()).isEqualTo("iPhone 15");
        assertThat(backendLotClient.submission.draft().description()).isEqualTo("256 GB, почти новый");
        assertThat(backendLotClient.submission.messageId()).isEqualTo(456);
        assertThat(backendLotClient.submission.photo()).containsExactly(1, 2, 3);
    }

    @Test
    void repeatsExpectedPromptForWrongMessageType() {
        route(callbackUpdate(123L, 77L, CallbackData.OFFER_LOT));

        SendMessage response = routeSingle(messageUpdate(messageWithPhoto(123L, 77L, 456, "photo-file-id")));

        assertThat(response.getText()).contains("название лота текстом");
        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> assertThat(state.step()).isEqualTo(ConversationStep.WAITING_TITLE));
        assertThat(backendLotClient.submission).isNull();
    }

    @Test
    void keepsPhotoStepWhenBackendFails() {
        backendLotClient.fail = true;
        route(callbackUpdate(123L, 77L, CallbackData.OFFER_LOT));
        route(messageUpdate(messageWithText(123L, 77L, "iPhone 15")));
        route(messageUpdate(messageWithText(123L, 77L, "256 GB, почти новый")));

        SendMessage response = routeSingle(messageUpdate(messageWithPhoto(123L, 77L, 456, "photo-file-id")));

        assertThat(response.getText()).contains("Не получилось отправить лот на оценку");
        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> assertThat(state.step()).isEqualTo(ConversationStep.WAITING_PHOTO));
    }

    @Test
    void startCommandResetsActiveConversation() {
        route(callbackUpdate(123L, 77L, CallbackData.OFFER_LOT));

        route(messageUpdate(messageWithText(123L, 77L, "/start")));

        assertThat(conversationStorage.findByUserId(77L)).isEmpty();
    }

    private SendMessage routeSingle(Update update) {
        List<BotApiMethod<?>> responses = route(update);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isInstanceOf(SendMessage.class);
        return (SendMessage) responses.get(0);
    }

    private List<BotApiMethod<?>> route(Update update) {
        return updateRouter.route(update);
    }

    private Update callbackUpdate(Long chatId, Long userId, String data) {
        Update update = new Update();
        update.setCallbackQuery(callbackQuery(chatId, userId, data));
        return update;
    }

    private CallbackQuery callbackQuery(Long chatId, Long userId, String data) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(data);
        callbackQuery.setFrom(user(userId));
        callbackQuery.setMessage(messageWithText(chatId, userId, "/start"));
        return callbackQuery;
    }

    private Update messageUpdate(Message message) {
        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private Message messageWithText(Long chatId, Long userId, String text) {
        Message message = baseMessage(chatId, userId);
        message.setText(text);
        return message;
    }

    private Message messageWithPhoto(Long chatId, Long userId, Integer messageId, String fileId) {
        PhotoSize photoSize = new PhotoSize();
        photoSize.setFileId(fileId);
        photoSize.setFileSize(100);

        Message message = baseMessage(chatId, userId);
        message.setMessageId(messageId);
        message.setPhoto(List.of(photoSize));
        return message;
    }

    private Message baseMessage(Long chatId, Long userId) {
        Chat chat = new Chat();
        chat.setId(chatId);

        Message message = new Message();
        message.setChat(chat);
        message.setFrom(user(userId));
        return message;
    }

    private User user(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUserName("test_user");
        return user;
    }

    private static class FakeTelegramFileDownloader implements TelegramFileDownloader {

        private String downloadedFileId;

        @Override
        public byte[] download(String fileId) {
            this.downloadedFileId = fileId;
            return new byte[]{1, 2, 3};
        }
    }

    private static class FakeBackendLotClient implements BackendLotClient {

        private boolean fail;
        private LotSubmission submission;

        @Override
        public BackendLot createLot(LotSubmission submission) {
            this.submission = submission;
            if (fail) {
                throw new IllegalStateException("backend is unavailable");
            }
            return new BackendLot("ad-1", 100);
        }
    }
}
