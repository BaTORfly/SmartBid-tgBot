package smartbid.tg.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import smartbid.tg.config.BackendProperties;

@Component
public class RestBackendLotClient implements BackendLotClient {

    private final RestClient restClient;

    public RestBackendLotClient(RestClient.Builder restClientBuilder, BackendProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.url().toString())
                .build();
    }

    @Override
    public BackendLot createLot(LotSubmission submission) {
        CreateAdRequest request = new CreateAdRequest(
                submission.draft().title(),
                submission.draft().chatId(),
                submission.messageId(),
                submission.draft().description(),
                submission.photo()
        );

        AdResponse response = restClient.post()
                .uri("/api/v1/ads")
                .body(request)
                .retrieve()
                .body(AdResponse.class);

        if (response == null) {
            throw new IllegalStateException("Backend returned empty ad response");
        }

        return new BackendLot(response.id(), response.price());
    }

    @Override
    public BackendAd findLotById(String adId) {
        AdResponse response = restClient.get()
                .uri("/api/v1/ads/{id}", adId)
                .retrieve()
                .body(AdResponse.class);

        if (response == null) {
            throw new IllegalStateException("Backend returned empty ad response");
        }

        return new BackendAd(
                response.id(),
                response.title(),
                response.chatId(),
                response.messageId(),
                response.description(),
                response.photo(),
                response.price(),
                response.pretendentId(),
                response.status(),
                response.publishedAt(),
                response.expiresAt()
        );
    }

    @Override
    public void publishLot(String adId, Long ownerChatId) {
        try {
            restClient.post()
                    .uri("/api/v1/ads/{id}/publish", adId)
                    .body(new PublishAdRequest(ownerChatId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException exception) {
            throw new IllegalStateException(
                    "Backend publish failed with status %s and body: %s".formatted(
                            exception.getStatusCode(),
                            exception.getResponseBodyAsString()
                    ),
                    exception
            );
        }
    }

    private record CreateAdRequest(
            String title,
            @JsonProperty("chat_id") Long chatId,
            @JsonProperty("message_id") Integer messageId,
            String description,
            byte[] photo
    ) {
    }

    private record PublishAdRequest(
            @JsonProperty("chat_id") Long chatId
    ) {
    }

    private record AdResponse(
            String id,
            String title,
            @JsonProperty("chat_id") Long chatId,
            @JsonProperty("message_id") Integer messageId,
            String description,
            byte[] photo,
            long price,
            @JsonProperty("pretendent_id") Long pretendentId,
            String status,
            @JsonProperty("published_at") java.time.OffsetDateTime publishedAt,
            @JsonProperty("expires_at") java.time.OffsetDateTime expiresAt
    ) {
    }
}
