package smartbid.tg.telegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.config.TelegramBotProperties;
import smartbid.tg.telegram.router.UpdateRouter;

import java.io.Serializable;

@Slf4j
public class SmartBidTelegramBot extends TelegramLongPollingBot implements TelegramClient {

    private final TelegramBotProperties properties;
    private final UpdateRouter updateRouter;

    public SmartBidTelegramBot(TelegramBotProperties properties, UpdateRouter updateRouter) {
        super(properties.token());
        this.properties = properties;
        this.updateRouter = updateRouter;
    }

    @Override
    public String getBotUsername() {
        return properties.username();
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateRouter.route(update).forEach(this::executeSafely);
    }

    private void executeSafely(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException exception) {
            log.warn("Failed to send Telegram response", exception);
        }
    }

    @Override
    public Message sendPhoto(SendPhoto photo) throws TelegramApiException {
        return execute(photo);
    }

    @Override
    public Message sendMessage(SendMessage message) throws TelegramApiException {
        return execute(message);
    }

    @Override
    public Serializable editMessageCaption(EditMessageCaption caption) throws TelegramApiException {
        return execute(caption);
    }
}
