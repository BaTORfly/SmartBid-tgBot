package smartbid.tg.infrastructure.telegram.handler.command;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {

    boolean supports(Message message);

    BotApiMethod<?> handle(Message message);
}
