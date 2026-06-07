package smartbid.tg.application;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.domain.InMemoryPublishedAuctionStorage;
import smartbid.tg.domain.PublishedAuction;
import smartbid.tg.infrastructure.backend.BackendAd;
import smartbid.tg.infrastructure.backend.BackendLot;
import smartbid.tg.infrastructure.backend.BackendLotClient;
import smartbid.tg.infrastructure.backend.BackendPriceUpdate;
import smartbid.tg.infrastructure.backend.LotSubmission;
import smartbid.tg.infrastructure.telegram.keyboard.AuctionPostKeyboardFactory;

import static org.assertj.core.api.Assertions.assertThat;

class BidServiceTest {

    private final FakeBackendLotClient backendLotClient = new FakeBackendLotClient();
    private final InMemoryPublishedAuctionStorage publishedAuctionStorage = new InMemoryPublishedAuctionStorage();
    private final AuctionPublicationServiceTest.FakeTelegramClient telegramClient =
            new AuctionPublicationServiceTest.FakeTelegramClient();
    private final BidService service = new BidService(
            backendLotClient,
            publishedAuctionStorage,
            new AuctionCaptionFactory(),
            new AuctionPostKeyboardFactory(),
            AuctionPublicationServiceTest.provider(telegramClient)
    );

    @Test
    void acceptsBidAndUpdatesCaptionAndStorage() {
        publishedAuctionStorage.save(auction());

        BidService.BidResult result = service.increase("ad-1", 77L, "@bidder");

        assertThat(result).isEqualTo(BidService.BidResult.ACCEPTED);
        assertThat(backendLotClient.increasedAdId).isEqualTo("ad-1");
        assertThat(backendLotClient.pretendentId).isEqualTo(77L);
        assertThat(publishedAuctionStorage.findByAdId("ad-1"))
                .hasValueSatisfying(auction -> {
                    assertThat(auction.currentPrice()).isEqualTo(105);
                    assertThat(auction.currentBidderId()).isEqualTo(77L);
                    assertThat(auction.currentBidderDisplayName()).isEqualTo("@bidder");
                });
        assertThat(telegramClient.editedCaption.getCaption()).contains("Текущая цена: 1.05 р");
        assertThat(telegramClient.editedCaption.getCaption()).contains("Текущий участник: @bidder");
        assertThat(telegramClient.editedCaption.getReplyMarkup()).isNotNull();
    }

    @Test
    void returnsNotFoundWhenAuctionMappingIsMissing() {
        BidService.BidResult result = service.increase("missing-ad", 77L, "@bidder");

        assertThat(result).isEqualTo(BidService.BidResult.NOT_FOUND);
        assertThat(backendLotClient.increasedAdId).isNull();
        assertThat(telegramClient.editedCaption).isNull();
    }

    @Test
    void returnsRejectedWhenBackendRejectsBid() {
        publishedAuctionStorage.save(auction());
        backendLotClient.fail = true;

        BidService.BidResult result = service.increase("ad-1", 77L, "@bidder");

        assertThat(result).isEqualTo(BidService.BidResult.REJECTED);
        assertThat(publishedAuctionStorage.findByAdId("ad-1"))
                .hasValueSatisfying(auction -> assertThat(auction.currentPrice()).isEqualTo(100));
        assertThat(telegramClient.editedCaption).isNull();
    }

    @Test
    void keepsAcceptedBidWhenTelegramEditFails() {
        publishedAuctionStorage.save(auction());
        telegramClient.failEditCaption = true;

        BidService.BidResult result = service.increase("ad-1", 77L, "@bidder");

        assertThat(result).isEqualTo(BidService.BidResult.ACCEPTED);
        assertThat(publishedAuctionStorage.findByAdId("ad-1"))
                .hasValueSatisfying(auction -> assertThat(auction.currentPrice()).isEqualTo(105));
    }

    private PublishedAuction auction() {
        return new PublishedAuction("ad-1", -100L, 789, "iPhone 15", "256 GB", null, 100, null, null);
    }

    private static class FakeBackendLotClient implements BackendLotClient {

        private boolean fail;
        private String increasedAdId;
        private Long pretendentId;

        @Override
        public BackendLot createLot(LotSubmission submission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendAd findLotById(String adId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void publishLot(String adId, Long ownerChatId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendPriceUpdate increaseLotPrice(String adId, Long pretendentId) {
            if (fail) {
                throw new IllegalStateException("bid rejected");
            }
            this.increasedAdId = adId;
            this.pretendentId = pretendentId;
            return new BackendPriceUpdate(adId, 105);
        }
    }
}
