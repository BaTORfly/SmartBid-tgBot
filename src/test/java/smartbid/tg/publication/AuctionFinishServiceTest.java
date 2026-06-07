package smartbid.tg.publication;

import org.junit.jupiter.api.Test;
import smartbid.tg.kafka.AdFinishedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class AuctionFinishServiceTest {

    private final InMemoryPublishedAuctionStorage publishedAuctionStorage = new InMemoryPublishedAuctionStorage();
    private final InMemoryProcessedEventStorage processedEventStorage = new InMemoryProcessedEventStorage();
    private final AuctionPublicationServiceTest.FakeTelegramClient telegramClient =
            new AuctionPublicationServiceTest.FakeTelegramClient();
    private final AuctionFinishService service = new AuctionFinishService(
            publishedAuctionStorage,
            processedEventStorage,
            new AuctionCaptionFactory(),
            telegramClient
    );

    @Test
    void editsPublishedAuctionCaptionWhenBought() {
        publishedAuctionStorage.save(new PublishedAuction("ad-1", -100L, 789));

        service.finish(new AdFinishedEvent("event-1", "ad-1", "bought", 77L, 105));

        assertThat(telegramClient.editedCaption.getChatId()).isEqualTo("-100");
        assertThat(telegramClient.editedCaption.getMessageId()).isEqualTo(789);
        assertThat(telegramClient.editedCaption.getCaption()).contains("Аукцион завершен");
        assertThat(telegramClient.editedCaption.getCaption()).contains("Telegram ID 77");
        assertThat(telegramClient.editedCaption.getCaption()).contains("Финальная цена: 1.05 р");
        assertThat(telegramClient.editedCaption.getReplyMarkup()).isNotNull();
        assertThat(processedEventStorage.isProcessed("event-1")).isTrue();
    }

    @Test
    void editsPublishedAuctionCaptionWhenExpired() {
        publishedAuctionStorage.save(new PublishedAuction("ad-1", -100L, 789));

        service.finish(new AdFinishedEvent("event-2", "ad-1", "expired", null, 100));

        assertThat(telegramClient.editedCaption.getCaption()).contains("без ставок");
        assertThat(telegramClient.editedCaption.getCaption()).contains("Финальная цена: 1.00 р");
    }

    @Test
    void editsPublishedAuctionCaptionWhenRemoved() {
        publishedAuctionStorage.save(new PublishedAuction("ad-1", -100L, 789));

        service.finish(new AdFinishedEvent("event-3", "ad-1", "removed", null, 100));

        assertThat(telegramClient.editedCaption.getCaption()).contains("снят с публикации");
    }

    @Test
    void skipsAlreadyProcessedEvent() {
        publishedAuctionStorage.save(new PublishedAuction("ad-1", -100L, 789));

        service.finish(new AdFinishedEvent("event-4", "ad-1", "expired", null, 100));
        telegramClient.editedCaption = null;
        service.finish(new AdFinishedEvent("event-4", "ad-1", "expired", null, 100));

        assertThat(telegramClient.editedCaption).isNull();
    }
}
