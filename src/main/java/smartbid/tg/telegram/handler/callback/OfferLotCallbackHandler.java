package smartbid.tg.telegram.handler.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import smartbid.tg.telegram.callback.CallbackData;

@Component
public class OfferLotCallbackHandler implements CallbackHandler {

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return CallbackData.OFFER_LOT.equals(callbackQuery.getData());
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        SendMessage response = new SendMessage();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setText("\u0421\u043a\u043e\u0440\u043e \u0437\u0434\u0435\u0441\u044c \u043f\u043e\u044f\u0432\u0438\u0442\u0441\u044f \u0444\u043e\u0440\u043c\u0430 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043b\u043e\u0442\u0430.");
        return response;
    }
}
