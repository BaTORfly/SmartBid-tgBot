package smartbid.tg.infrastructure.telegram.bot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import smartbid.tg.configuration.TelegramBotProperties;
import smartbid.tg.infrastructure.telegram.router.UpdateRouter;

@Configuration
public class TelegramBotConfiguration {

    @Bean
    @ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
    TelegramBotsApi telegramBotsApi(SmartBidTelegramBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }

    @Bean
    SmartBidTelegramBot smartBidTelegramBot(TelegramBotProperties properties, UpdateRouter updateRouter) {
        return new SmartBidTelegramBot(properties, updateRouter);
    }
}
