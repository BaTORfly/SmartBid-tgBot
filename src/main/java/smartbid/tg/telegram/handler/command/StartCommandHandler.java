package smartbid.tg.telegram.handler.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import smartbid.tg.telegram.keyboard.StartKeyboardFactory;

@Component
public class StartCommandHandler implements CommandHandler {

    private static final String START_COMMAND = "/start";

    private final StartKeyboardFactory keyboardFactory;

    public StartCommandHandler(StartKeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(Message message) {
        return START_COMMAND.equals(message.getText());
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("""
                \u041f\u0440\u0438\u0432\u0435\u0442! \u042f \u0431\u043e\u0442 SmartBid.

                \u0417\u0434\u0435\u0441\u044c \u043c\u043e\u0436\u043d\u043e \u043f\u0440\u0435\u0434\u043b\u043e\u0436\u0438\u0442\u044c \u043b\u043e\u0442 \u0434\u043b\u044f \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430: \u0431\u043e\u0442 \u0441\u043e\u0431\u0435\u0440\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0435, \u043e\u0442\u043f\u0440\u0430\u0432\u0438\u0442 \u0438\u0445 \u043d\u0430 \u043e\u0446\u0435\u043d\u043a\u0443 \u0438 \u043f\u043e\u043c\u043e\u0436\u0435\u0442 \u043e\u043f\u0443\u0431\u043b\u0438\u043a\u043e\u0432\u0430\u0442\u044c \u043b\u043e\u0442 \u0432 \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u0435.
                """);
        response.setReplyMarkup(keyboardFactory.create());
        return response;
    }
}
