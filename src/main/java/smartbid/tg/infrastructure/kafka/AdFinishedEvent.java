package smartbid.tg.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdFinishedEvent(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ad_id") String adId,
        String status,
        @JsonProperty("pretendent_id") Long pretendentId,
        @JsonProperty("final_price") long finalPrice
) {
}
