package smartbid.tg.domain;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryProcessedEventStorage implements ProcessedEventStorage {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        processedEventIds.add(eventId);
    }
}
