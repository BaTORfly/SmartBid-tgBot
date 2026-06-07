package smartbid.tg.domain;

import java.time.OffsetDateTime;

public record PublishedAuction(
        String adId,
        Long targetChatId,
        Integer targetMessageId,
        String title,
        String description,
        OffsetDateTime expiresAt,
        long currentPrice,
        Long currentBidderId,
        String currentBidderDisplayName
) {

    public PublishedAuction withBid(long price, Long bidderId, String bidderDisplayName) {
        return new PublishedAuction(
                adId,
                targetChatId,
                targetMessageId,
                title,
                description,
                expiresAt,
                price,
                bidderId,
                bidderDisplayName
        );
    }
}
