package smartbid.tg.domain;

import java.util.Optional;

public interface PublishedAuctionStorage {

    Optional<PublishedAuction> findByAdId(String adId);

    void save(PublishedAuction auction);
}
