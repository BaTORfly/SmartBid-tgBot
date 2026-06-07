package smartbid.tg.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import smartbid.tg.application.AuctionFinishService;

@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AdFinishedEventListener {

    private final ObjectMapper objectMapper;
    private final AuctionFinishService finishService;

    public AdFinishedEventListener(ObjectMapper objectMapper, AuctionFinishService finishService) {
        this.objectMapper = objectMapper;
        this.finishService = finishService;
    }

    @KafkaListener(
            topics = "${kafka.ad-finished-topic}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(String payload) {
        AdFinishedEvent event = parse(payload);
        log.info("Received ad finished event: eventId={}, adId={}, status={}", event.eventId(), event.adId(), event.status());
        finishService.finish(event);
    }

    private AdFinishedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, AdFinishedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid ad finished event payload", exception);
        }
    }
}
