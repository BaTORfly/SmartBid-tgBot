package smartbid.tg.telegram.callback;

public final class CallbackData {

    public static final String OFFER_LOT = "lot:offer";
    public static final String RETRY_LOT = "lot:retry";
    public static final String PUBLISH_LOT_PREFIX = "lot:publish:";
    public static final String INCREASE_BID_PREFIX = "bid:increase:";
    public static final String FINISHED_AUCTION_PREFIX = "auction:finished:";

    private CallbackData() {
    }

    public static String publishLot(String adId) {
        return PUBLISH_LOT_PREFIX + adId;
    }

    public static boolean isPublishLot(String data) {
        return data != null && data.startsWith(PUBLISH_LOT_PREFIX);
    }

    public static String publishLotAdId(String data) {
        return data.substring(PUBLISH_LOT_PREFIX.length());
    }

    public static String increaseBid(String adId) {
        return INCREASE_BID_PREFIX + adId;
    }

    public static String finishedAuction(String adId) {
        return FINISHED_AUCTION_PREFIX + adId;
    }
}
