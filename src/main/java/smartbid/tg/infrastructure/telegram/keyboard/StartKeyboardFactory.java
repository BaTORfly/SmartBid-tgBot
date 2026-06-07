package smartbid.tg.infrastructure.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import smartbid.tg.infrastructure.telegram.callback.CallbackData;

import java.util.List;

@Component
public class StartKeyboardFactory {

    public InlineKeyboardMarkup create() {
        InlineKeyboardButton offerLotButton = new InlineKeyboardButton();
        offerLotButton.setText("Предложить лот для публикации");
        offerLotButton.setCallbackData(CallbackData.OFFER_LOT);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(offerLotButton)));
        return markup;
    }
}
