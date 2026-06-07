package smartbid.tg.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdCreatedEvent(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("ad_id") String adId
) {
}
