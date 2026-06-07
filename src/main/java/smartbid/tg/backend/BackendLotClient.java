package smartbid.tg.backend;

public interface BackendLotClient {

    BackendLot createLot(LotSubmission submission);

    BackendAd findLotById(String adId);

    void publishLot(String adId, Long ownerChatId);

    BackendPriceUpdate increaseLotPrice(String adId, Long pretendentId);
}
