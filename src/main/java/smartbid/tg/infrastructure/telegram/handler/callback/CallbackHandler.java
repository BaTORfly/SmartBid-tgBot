package smartbid.tg.infrastructure.telegram.handler.callback;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackHandler {

    boolean supports(CallbackQuery callbackQuery);

    BotApiMethod<?> handle(CallbackQuery callbackQuery);
}
