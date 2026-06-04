package smartbid.tg.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import smartbid.tg.telegram.callback.CallbackData;

import java.util.List;

@Component
public class StartKeyboardFactory {

    public InlineKeyboardMarkup create() {
        InlineKeyboardButton offerLotButton = new InlineKeyboardButton();
        offerLotButton.setText("\u041f\u0440\u0435\u0434\u043b\u043e\u0436\u0438\u0442\u044c \u043b\u043e\u0442 \u0434\u043b\u044f \u043f\u0443\u0431\u043b\u0438\u043a\u0430\u0446\u0438\u0438");
        offerLotButton.setCallbackData(CallbackData.OFFER_LOT);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(offerLotButton)));
        return markup;
    }
}
