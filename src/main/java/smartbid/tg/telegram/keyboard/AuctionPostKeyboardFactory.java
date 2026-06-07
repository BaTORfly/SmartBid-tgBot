package smartbid.tg.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import smartbid.tg.telegram.callback.CallbackData;

import java.util.List;

@Component
public class AuctionPostKeyboardFactory {

    public InlineKeyboardMarkup create(String adId) {
        InlineKeyboardButton increaseButton = new InlineKeyboardButton();
        increaseButton.setText("Повысить лот");
        increaseButton.setCallbackData(CallbackData.increaseBid(adId));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(increaseButton)));
        return markup;
    }
}
