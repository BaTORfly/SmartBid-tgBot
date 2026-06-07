package smartbid.tg.domain;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryPublishedAuctionStorage implements PublishedAuctionStorage {

    private final ConcurrentMap<String, PublishedAuction> auctions = new ConcurrentHashMap<>();

    @Override
    public Optional<PublishedAuction> findByAdId(String adId) {
        return Optional.ofNullable(auctions.get(adId));
    }

    @Override
    public void save(PublishedAuction auction) {
        auctions.put(auction.adId(), auction);
    }
}
