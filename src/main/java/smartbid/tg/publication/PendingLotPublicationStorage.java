package smartbid.tg.publication;

import java.util.Optional;

public interface PendingLotPublicationStorage {

    Optional<PendingLotPublication> findByAdId(String adId);

    Optional<PendingLotPublication> findByOwnerUserId(Long ownerUserId);

    void save(PendingLotPublication publication);

    void deleteByAdId(String adId);

    void deleteByOwnerUserId(Long ownerUserId);
}
