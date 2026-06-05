package smartbid.tg.lot;

public record LotDraft(
        Long telegramUserId,
        Long chatId,
        String title,
        String description,
        String photoFileId
) {

    public static LotDraft empty(Long telegramUserId, Long chatId) {
        return new LotDraft(telegramUserId, chatId, null, null, null);
    }

    public LotDraft withTitle(String title) {
        return new LotDraft(telegramUserId, chatId, title, description, photoFileId);
    }

    public LotDraft withDescription(String description) {
        return new LotDraft(telegramUserId, chatId, title, description, photoFileId);
    }

    public LotDraft withPhotoFileId(String photoFileId) {
        return new LotDraft(telegramUserId, chatId, title, description, photoFileId);
    }
}
