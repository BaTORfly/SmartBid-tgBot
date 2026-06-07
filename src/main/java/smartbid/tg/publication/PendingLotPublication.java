package smartbid.tg.publication;

public record PendingLotPublication(
        String adId,
        Long ownerChatId,
        Long ownerUserId,
        String title,
        String description,
        String photoFileId,
        long price
) {
}
