package smartbid.tg.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyFormatterTest {

    @Test
    void formatsKopecksAsRubles() {
        assertThat(MoneyFormatter.rublesFromKopecks(100)).isEqualTo("1.00");
        assertThat(MoneyFormatter.rublesFromKopecks(1)).isEqualTo("0.01");
        assertThat(MoneyFormatter.rublesFromKopecks(12345)).isEqualTo("123.45");
    }
}
