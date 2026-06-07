package smartbid.tg.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.backend.BackendAd;
import smartbid.tg.backend.BackendLotClient;
import smartbid.tg.config.TelegramProperties;
import smartbid.tg.telegram.bot.TelegramClient;
import smartbid.tg.telegram.keyboard.AuctionPostKeyboardFactory;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionPublicationService {

    private final BackendLotClient backendLotClient;
    private final TelegramProperties telegramProperties;
    private final TelegramClient telegramClient;
    private final AuctionPostKeyboardFactory keyboardFactory;
    private final PublishedAuctionStorage publishedAuctionStorage;
    private final AuctionCaptionFactory captionFactory;

    public AuctionPublicationService(
            BackendLotClient backendLotClient,
            TelegramProperties telegramProperties,
            TelegramClient telegramClient,
            AuctionPostKeyboardFactory keyboardFactory,
            PublishedAuctionStorage publishedAuctionStorage,
            AuctionCaptionFactory captionFactory
    ) {
        this.backendLotClient = backendLotClient;
        this.telegramProperties = telegramProperties;
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
        this.publishedAuctionStorage = publishedAuctionStorage;
        this.captionFactory = captionFactory;
    }

    public void publish(String adId) {
        BackendAd ad = backendLotClient.findLotById(adId);
        if (ad.photo() == null || ad.photo().length == 0) {
            throw new IllegalStateException("Backend ad has no photo: " + adId);
        }

        Message publishedMessage = sendAuctionPost(ad);
        publishedAuctionStorage.save(new PublishedAuction(
                ad.id(),
                telegramProperties.targetChatId(),
                publishedMessage.getMessageId()
        ));
        sendOwnerConfirmation(ad);
    }

    private Message sendAuctionPost(BackendAd ad) {
        SendPhoto request = new SendPhoto();
        request.setChatId(telegramProperties.targetChatId());
        request.setPhoto(new InputFile(new ByteArrayInputStream(ad.photo()), ad.id() + ".jpg"));
        request.setCaption(captionFactory.active(ad));
        request.setReplyMarkup(keyboardFactory.create(ad.id()));

        try {
            return telegramClient.sendPhoto(request);
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to publish auction post to Telegram", exception);
        }
    }

    private void sendOwnerConfirmation(BackendAd ad) {
        if (ad.chatId() == null) {
            return;
        }

        SendMessage confirmation = new SendMessage();
        confirmation.setChatId(ad.chatId());
        confirmation.setText("Лот опубликован в сообществе.");

        try {
            telegramClient.sendMessage(confirmation);
        } catch (TelegramApiException exception) {
            log.warn("Failed to send owner publication confirmation for ad {}", ad.id(), exception);
        }
    }
}
