package smartbid.tg.infrastructure.backend;

import java.time.OffsetDateTime;

public record BackendAd(
        String id,
        String title,
        Long chatId,
        Integer messageId,
        String description,
        byte[] photo,
        long price,
        Long pretendentId,
        String status,
        OffsetDateTime publishedAt,
        OffsetDateTime expiresAt
) {
}
