package smartbid.tg.infrastructure.telegram.handler.message;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {

    boolean supports(Message message);

    BotApiMethod<?> handle(Message message);
}
