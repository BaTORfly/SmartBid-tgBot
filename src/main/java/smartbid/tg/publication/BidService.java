package smartbid.tg.publication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.backend.BackendLotClient;
import smartbid.tg.backend.BackendPriceUpdate;
import smartbid.tg.telegram.bot.TelegramClient;
import smartbid.tg.telegram.keyboard.AuctionPostKeyboardFactory;

@Slf4j
@Service
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class BidService {

    private final BackendLotClient backendLotClient;
    private final PublishedAuctionStorage publishedAuctionStorage;
    private final AuctionCaptionFactory captionFactory;
    private final AuctionPostKeyboardFactory keyboardFactory;
    private final ObjectProvider<TelegramClient> telegramClientProvider;

    public BidService(
            BackendLotClient backendLotClient,
            PublishedAuctionStorage publishedAuctionStorage,
            AuctionCaptionFactory captionFactory,
            AuctionPostKeyboardFactory keyboardFactory,
            ObjectProvider<TelegramClient> telegramClientProvider
    ) {
        this.backendLotClient = backendLotClient;
        this.publishedAuctionStorage = publishedAuctionStorage;
        this.captionFactory = captionFactory;
        this.keyboardFactory = keyboardFactory;
        this.telegramClientProvider = telegramClientProvider;
    }

    public BidResult increase(String adId, Long bidderId, String bidderDisplayName) {
        PublishedAuction auction = publishedAuctionStorage.findByAdId(adId).orElse(null);
        if (auction == null) {
            return BidResult.notFound();
        }

        BackendPriceUpdate update;
        try {
            update = backendLotClient.increaseLotPrice(adId, bidderId);
        } catch (RuntimeException exception) {
            log.warn("Failed to increase bid for ad {} by user {}: {}", adId, bidderId, exception.getMessage());
            return BidResult.rejected();
        }

        PublishedAuction updatedAuction = auction.withBid(update.price(), bidderId, bidderDisplayName);
        publishedAuctionStorage.save(updatedAuction);
        try {
            editCaption(updatedAuction);
        } catch (RuntimeException exception) {
            log.warn("Bid for ad {} was accepted, but Telegram post update failed: {}", adId, exception.getMessage());
        }
        return BidResult.accepted();
    }

    private void editCaption(PublishedAuction auction) {
        EditMessageCaption request = new EditMessageCaption();
        request.setChatId(auction.targetChatId());
        request.setMessageId(auction.targetMessageId());
        request.setCaption(captionFactory.active(auction));
        request.setReplyMarkup(keyboardFactory.create(auction.adId()));

        try {
            telegramClientProvider.getObject().editMessageCaption(request);
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to edit auction post after bid", exception);
        }
    }

    public enum BidResult {
        ACCEPTED,
        REJECTED,
        NOT_FOUND;

        static BidResult accepted() {
            return ACCEPTED;
        }

        static BidResult rejected() {
            return REJECTED;
        }

        static BidResult notFound() {
            return NOT_FOUND;
        }
    }
}
