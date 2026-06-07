package smartbid.tg.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kafka")
public record SmartBidKafkaProperties(
        @NotBlank String bootstrapServers,
        @NotBlank String adCreatedTopic,
        @NotBlank String adFinishedTopic,
        @Valid Consumer consumer
) {

    public record Consumer(
            boolean enabled,
            @NotBlank String groupId
    ) {
    }
}
