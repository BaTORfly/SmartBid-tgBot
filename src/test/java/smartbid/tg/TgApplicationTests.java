package smartbid.tg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"telegram.bot.enabled=false",
		"telegram.bot.token=test-token",
		"telegram.bot.username=test_bot",
		"kafka.consumer.enabled=false"
})
class TgApplicationTests {

	@Test
	void contextLoads() {
	}

}
