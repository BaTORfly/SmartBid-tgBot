package smartbid.tg.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.kafka.AdFinishedEvent;
import smartbid.tg.telegram.callback.CallbackData;
import smartbid.tg.telegram.bot.TelegramClient;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionFinishService {

    private final PublishedAuctionStorage publishedAuctionStorage;
    private final ProcessedEventStorage processedEventStorage;
    private final AuctionCaptionFactory captionFactory;
    private final ObjectProvider<TelegramClient> telegramClientProvider;

    public AuctionFinishService(
            PublishedAuctionStorage publishedAuctionStorage,
            ProcessedEventStorage processedEventStorage,
            AuctionCaptionFactory captionFactory,
            ObjectProvider<TelegramClient> telegramClientProvider
    ) {
        this.publishedAuctionStorage = publishedAuctionStorage;
        this.processedEventStorage = processedEventStorage;
        this.captionFactory = captionFactory;
        this.telegramClientProvider = telegramClientProvider;
    }

    public void finish(AdFinishedEvent event) {
        if (processedEventStorage.isProcessed(event.eventId())) {
            log.info("Skip already processed ad finished event: eventId={}", event.eventId());
            return;
        }

        PublishedAuction auction = publishedAuctionStorage.findByAdId(event.adId()).orElse(null);
        if (auction == null) {
            log.warn("Published auction mapping was not found for ad {}", event.adId());
            return;
        }

        editCaption(auction, event);
        processedEventStorage.markProcessed(event.eventId());
    }

    private void editCaption(PublishedAuction auction, AdFinishedEvent event) {
        EditMessageCaption request = new EditMessageCaption();
        request.setChatId(auction.targetChatId());
        request.setMessageId(auction.targetMessageId());
        request.setCaption(captionFactory.finished(event, auction));
        request.setReplyMarkup(finishedMarkup(event.adId()));

        try {
            telegramClientProvider.getObject().editMessageCaption(request);
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to edit finished auction post", exception);
        }
    }

    private InlineKeyboardMarkup finishedMarkup(String adId) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Аукцион завершен");
        button.setCallbackData(CallbackData.finishedAuction(adId));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(button)));
        return markup;
    }
}
