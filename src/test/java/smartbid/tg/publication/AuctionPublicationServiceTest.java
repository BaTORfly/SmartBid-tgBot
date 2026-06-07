package smartbid.tg.publication;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import smartbid.tg.backend.BackendAd;
import smartbid.tg.backend.BackendLot;
import smartbid.tg.backend.BackendLotClient;
import smartbid.tg.backend.LotSubmission;
import smartbid.tg.config.TelegramProperties;
import smartbid.tg.telegram.bot.TelegramClient;
import smartbid.tg.telegram.keyboard.AuctionPostKeyboardFactory;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionPublicationServiceTest {

    private static final OffsetDateTime EXPIRES_AT = OffsetDateTime.parse("2026-06-07T15:30:00Z");

    private final FakeBackendLotClient backendLotClient = new FakeBackendLotClient();
    private final FakeTelegramClient telegramClient = new FakeTelegramClient();
    private final InMemoryPublishedAuctionStorage publishedAuctionStorage = new InMemoryPublishedAuctionStorage();
    private final AuctionPublicationService service = new AuctionPublicationService(
            backendLotClient,
            new TelegramProperties(-100L),
            telegramClient,
            new AuctionPostKeyboardFactory(),
            publishedAuctionStorage,
            new AuctionCaptionFactory()
    );

    @Test
    void publishesAuctionPostAndStoresMapping() {
        service.publish("ad-1");

        assertThat(backendLotClient.requestedAdId).isEqualTo("ad-1");
        assertThat(telegramClient.sentPhoto.getChatId()).isEqualTo("-100");
        assertThat(telegramClient.sentPhoto.getCaption()).contains("iPhone 15");
        assertThat(telegramClient.sentPhoto.getCaption()).contains("Текущая цена: 1.00 р");
        assertThat(telegramClient.sentPhoto.getCaption()).contains("Завершение: 07.06.2026 18:30 МСК");
        assertThat(telegramClient.sentPhoto.getReplyMarkup()).isNotNull();
        assertThat(publishedAuctionStorage.findByAdId("ad-1"))
                .hasValueSatisfying(auction -> {
                    assertThat(auction.targetChatId()).isEqualTo(-100L);
                    assertThat(auction.targetMessageId()).isEqualTo(789);
                });
        assertThat(telegramClient.sentMessage.getChatId()).isEqualTo("123");
        assertThat(telegramClient.sentMessage.getText()).contains("Лот опубликован");
    }

    @Test
    void usesFallbackWhenExpirationTimeIsMissing() {
        backendLotClient.expiresAt = null;

        service.publish("ad-1");

        assertThat(telegramClient.sentPhoto.getCaption()).contains("Завершение: уточняется");
    }

    @Test
    void propagatesTelegramPostFailureForKafkaRetry() {
        telegramClient.failPhoto = true;

        assertThatThrownBy(() -> service.publish("ad-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to publish auction post");
        assertThat(publishedAuctionStorage.findByAdId("ad-1")).isEmpty();
    }

    private static class FakeBackendLotClient implements BackendLotClient {

        private String requestedAdId;
        private OffsetDateTime expiresAt = EXPIRES_AT;

        @Override
        public BackendLot createLot(LotSubmission submission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackendAd findLotById(String adId) {
            this.requestedAdId = adId;
            return new BackendAd(
                    adId,
                    "iPhone 15",
                    123L,
                    456,
                    "256 GB",
                    new byte[]{1, 2, 3},
                    100,
                    null,
                    "published",
                    OffsetDateTime.parse("2026-06-06T15:30:00Z"),
                    expiresAt
            );
        }

        @Override
        public void publishLot(String adId, Long ownerChatId) {
            throw new UnsupportedOperationException();
        }
    }

    static class FakeTelegramClient implements TelegramClient {

        boolean failPhoto;
        SendPhoto sentPhoto;
        SendMessage sentMessage;
        EditMessageCaption editedCaption;

        @Override
        public Message sendPhoto(SendPhoto photo) throws TelegramApiException {
            if (failPhoto) {
                throw new TelegramApiException("failed");
            }
            this.sentPhoto = photo;
            Message message = new Message();
            message.setMessageId(789);
            Chat chat = new Chat();
            chat.setId(-100L);
            message.setChat(chat);
            return message;
        }

        @Override
        public Message sendMessage(SendMessage message) {
            this.sentMessage = message;
            Message response = new Message();
            response.setMessageId(790);
            return response;
        }

        @Override
        public Serializable editMessageCaption(EditMessageCaption caption) {
            this.editedCaption = caption;
            return Boolean.TRUE;
        }
    }
}
