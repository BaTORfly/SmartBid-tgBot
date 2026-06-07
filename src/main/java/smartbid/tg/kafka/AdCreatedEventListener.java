package smartbid.tg.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import smartbid.tg.publication.AuctionPublicationService;

@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AdCreatedEventListener {

    private final ObjectMapper objectMapper;
    private final AuctionPublicationService publicationService;

    public AdCreatedEventListener(ObjectMapper objectMapper, AuctionPublicationService publicationService) {
        this.objectMapper = objectMapper;
        this.publicationService = publicationService;
    }

    @KafkaListener(
            topics = "${kafka.ad-created-topic}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(String payload) {
        AdCreatedEvent event = parse(payload);
        log.info("Received ad created event: eventId={}, adId={}", event.eventId(), event.adId());
        publicationService.publish(event.adId());
    }

    private AdCreatedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, AdCreatedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid ad created event payload", exception);
        }
    }
}
