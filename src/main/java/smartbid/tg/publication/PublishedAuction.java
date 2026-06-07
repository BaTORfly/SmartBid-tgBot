package smartbid.tg.publication;

public record PublishedAuction(
        String adId,
        Long targetChatId,
        Integer targetMessageId
) {
}
