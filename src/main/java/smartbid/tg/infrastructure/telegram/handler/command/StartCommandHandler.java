package smartbid.tg.infrastructure.telegram.handler.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import smartbid.tg.domain.ConversationStorage;
import smartbid.tg.infrastructure.telegram.keyboard.StartKeyboardFactory;

@Component
public class StartCommandHandler implements CommandHandler {

    private static final String START_COMMAND = "/start";

    private final StartKeyboardFactory keyboardFactory;
    private final ConversationStorage conversationStorage;

    public StartCommandHandler(StartKeyboardFactory keyboardFactory, ConversationStorage conversationStorage) {
        this.keyboardFactory = keyboardFactory;
        this.conversationStorage = conversationStorage;
    }

    @Override
    public boolean supports(Message message) {
        return START_COMMAND.equals(message.getText());
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        conversationStorage.deleteByUserId(message.getFrom().getId());

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("""
                Привет! Я бот SmartBid.

                Здесь можно предложить лот для аукциона: бот соберет данные, отправит их на оценку и поможет опубликовать лот в сообществе.
                """);
        response.setReplyMarkup(keyboardFactory.create());
        return response;
    }
}
