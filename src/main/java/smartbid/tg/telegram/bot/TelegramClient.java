package smartbid.tg.telegram.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

public interface TelegramClient {

    Message sendPhoto(SendPhoto photo) throws TelegramApiException;

    Message sendMessage(SendMessage message) throws TelegramApiException;

    Serializable editMessageCaption(EditMessageCaption caption) throws TelegramApiException;
}
