package smartbid.tg.infrastructure.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import smartbid.tg.infrastructure.telegram.callback.CallbackData;

import java.util.List;

@Component
public class PostEvaluationKeyboardFactory {

    public InlineKeyboardMarkup create(String adId) {
        InlineKeyboardButton publishButton = new InlineKeyboardButton();
        publishButton.setText("Выставить на аукцион");
        publishButton.setCallbackData(CallbackData.publishLot(adId));

        InlineKeyboardButton retryButton = new InlineKeyboardButton();
        retryButton.setText("Предложить другой лот");
        retryButton.setCallbackData(CallbackData.RETRY_LOT);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(publishButton), List.of(retryButton)));
        return markup;
    }
}
