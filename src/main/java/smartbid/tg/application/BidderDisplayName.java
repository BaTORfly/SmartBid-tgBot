package smartbid.tg.application;

import org.telegram.telegrambots.meta.api.objects.User;

public final class BidderDisplayName {

    private BidderDisplayName() {
    }

    public static String from(User user) {
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            return "@" + user.getUserName();
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String displayName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        if (!displayName.isBlank()) {
            return displayName;
        }

        return "Telegram ID " + user.getId();
    }
}
