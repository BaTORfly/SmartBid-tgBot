package smartbid.tg.telegram.router;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import smartbid.tg.telegram.callback.CallbackData;
import smartbid.tg.telegram.handler.callback.OfferLotCallbackHandler;
import smartbid.tg.telegram.handler.command.StartCommandHandler;
import smartbid.tg.telegram.keyboard.StartKeyboardFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateRouterTest {

    private final UpdateRouter updateRouter = new UpdateRouter(
            List.of(new StartCommandHandler(new StartKeyboardFactory())),
            List.of(new OfferLotCallbackHandler())
    );

    @Test
    void handlesStartCommand() {
        Update update = new Update();
        Message message = messageWithText(123L, "/start");
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
    void ignoresUnknownText() {
        Update update = new Update();
        update.setMessage(messageWithText(123L, "hello"));

        List<BotApiMethod<?>> responses = updateRouter.route(update);

        assertThat(responses).isEmpty();
    }

    @Test
    void handlesOfferLotCallbackWithStubResponse() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(CallbackData.OFFER_LOT);
        callbackQuery.setMessage(messageWithText(123L, "/start"));
        update.setCallbackQuery(callbackQuery);

        List<BotApiMethod<?>> responses = updateRouter.route(update);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isInstanceOf(SendMessage.class);

        SendMessage response = (SendMessage) responses.get(0);
        assertThat(response.getChatId()).isEqualTo("123");
        assertThat(response.getText()).contains("\u0444\u043e\u0440\u043c\u0430 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043b\u043e\u0442\u0430");
    }

    private Message messageWithText(Long chatId, String text) {
        Chat chat = new Chat();
        chat.setId(chatId);

        Message message = new Message();
        message.setChat(chat);
        message.setText(text);
        return message;
    }
}
