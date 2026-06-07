package smartbid.tg.domain;

public interface ProcessedEventStorage {

    boolean isProcessed(String eventId);

    void markProcessed(String eventId);
}
