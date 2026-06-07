package smartbid.tg.publication;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryPendingLotPublicationStorage implements PendingLotPublicationStorage {

    private final ConcurrentMap<String, PendingLotPublication> byAdId = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, String> adIdByOwnerUserId = new ConcurrentHashMap<>();

    @Override
    public Optional<PendingLotPublication> findByAdId(String adId) {
        return Optional.ofNullable(byAdId.get(adId));
    }

    @Override
    public Optional<PendingLotPublication> findByOwnerUserId(Long ownerUserId) {
        return Optional.ofNullable(adIdByOwnerUserId.get(ownerUserId))
                .map(byAdId::get);
    }

    @Override
    public void save(PendingLotPublication publication) {
        findByOwnerUserId(publication.ownerUserId())
                .map(PendingLotPublication::adId)
                .ifPresent(byAdId::remove);
        byAdId.put(publication.adId(), publication);
        adIdByOwnerUserId.put(publication.ownerUserId(), publication.adId());
    }

    @Override
    public void deleteByAdId(String adId) {
        PendingLotPublication removed = byAdId.remove(adId);
        if (removed != null) {
            adIdByOwnerUserId.remove(removed.ownerUserId(), adId);
        }
    }

    @Override
    public void deleteByOwnerUserId(Long ownerUserId) {
        String adId = adIdByOwnerUserId.remove(ownerUserId);
        if (adId != null) {
            byAdId.remove(adId);
        }
    }
}
