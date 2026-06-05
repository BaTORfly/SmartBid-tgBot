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
import smartbid.tg.conversation.ConversationStep;
import smartbid.tg.conversation.InMemoryConversationStorage;
import smartbid.tg.telegram.callback.CallbackData;
import smartbid.tg.telegram.handler.callback.OfferLotCallbackHandler;
import smartbid.tg.telegram.handler.command.StartCommandHandler;
import smartbid.tg.telegram.handler.message.LotDraftMessageHandler;
import smartbid.tg.telegram.keyboard.StartKeyboardFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateRouterTest {

    private final InMemoryConversationStorage conversationStorage = new InMemoryConversationStorage();
    private final UpdateRouter updateRouter = new UpdateRouter(
            List.of(new StartCommandHandler(new StartKeyboardFactory(), conversationStorage)),
            List.of(new OfferLotCallbackHandler(conversationStorage)),
            List.of(new LotDraftMessageHandler(conversationStorage))
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
    void collectsTitleDescriptionAndPhoto() {
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

        SendMessage photoResponse = routeSingle(messageUpdate(messageWithPhoto(123L, 77L, "photo-file-id")));
        assertThat(photoResponse.getText()).contains("Черновик лота собран");
        assertThat(photoResponse.getText()).contains("iPhone 15");
        assertThat(conversationStorage.findByUserId(77L)).isEmpty();
    }

    @Test
    void repeatsExpectedPromptForWrongMessageType() {
        route(callbackUpdate(123L, 77L, CallbackData.OFFER_LOT));

        SendMessage response = routeSingle(messageUpdate(messageWithPhoto(123L, 77L, "photo-file-id")));

        assertThat(response.getText()).contains("название лота текстом");
        assertThat(conversationStorage.findByUserId(77L))
                .hasValueSatisfying(state -> assertThat(state.step()).isEqualTo(ConversationStep.WAITING_TITLE));
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

    private Message messageWithPhoto(Long chatId, Long userId, String fileId) {
        PhotoSize photoSize = new PhotoSize();
        photoSize.setFileId(fileId);
        photoSize.setFileSize(100);

        Message message = baseMessage(chatId, userId);
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
}
