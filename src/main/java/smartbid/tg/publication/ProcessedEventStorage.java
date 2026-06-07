package smartbid.tg.publication;

public interface ProcessedEventStorage {

    boolean isProcessed(String eventId);

    void markProcessed(String eventId);
}
