package smartbid.tg.backend;

import smartbid.tg.lot.LotDraft;

public record LotSubmission(
        LotDraft draft,
        byte[] photo,
        Integer messageId
) {
}
