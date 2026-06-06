package smartbid.tg.common;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String rublesFromKopecks(long kopecks) {
        long rubles = kopecks / 100;
        long remainingKopecks = Math.abs(kopecks % 100);
        return "%d.%02d".formatted(rubles, remainingKopecks);
    }
}
