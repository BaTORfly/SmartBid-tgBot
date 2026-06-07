package smartbid.tg.infrastructure.backend;

import smartbid.tg.domain.LotDraft;

public record LotSubmission(
        LotDraft draft,
        byte[] photo,
        Integer messageId
) {
}
