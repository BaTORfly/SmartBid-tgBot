package smartbid.tg.application;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.assertj.core.api.Assertions.assertThat;

class BidderDisplayNameTest {

    @Test
    void usesUsernameWhenPresent() {
        User user = user(77L);
        user.setUserName("bidder");
        user.setFirstName("Ivan");

        assertThat(BidderDisplayName.from(user)).isEqualTo("@bidder");
    }

    @Test
    void usesFirstAndLastNameWhenUsernameIsMissing() {
        User user = user(77L);
        user.setFirstName("Ivan");
        user.setLastName("Petrov");

        assertThat(BidderDisplayName.from(user)).isEqualTo("Ivan Petrov");
    }

    @Test
    void usesTelegramIdWhenNamesAreMissing() {
        User user = user(77L);

        assertThat(BidderDisplayName.from(user)).isEqualTo("Telegram ID 77");
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
