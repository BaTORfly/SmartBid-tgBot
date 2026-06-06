package smartbid.tg.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
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

    private record CreateAdRequest(
            String title,
            @JsonProperty("chat_id") Long chatId,
            @JsonProperty("message_id") Integer messageId,
            String description,
            byte[] photo
    ) {
    }

    private record AdResponse(
            String id,
            long price
    ) {
    }
}
