package smartbid.tg.infrastructure.telegram.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import smartbid.tg.application.BidService;
import smartbid.tg.application.BidderDisplayName;
import smartbid.tg.infrastructure.telegram.callback.CallbackData;

@Slf4j
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class IncreaseBidCallbackHandler implements CallbackHandler {

    private final BidService bidService;

    public IncreaseBidCallbackHandler(BidService bidService) {
        this.bidService = bidService;
    }

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return CallbackData.isIncreaseBid(callbackQuery.getData());
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        String adId = CallbackData.increaseBidAdId(callbackQuery.getData());
        Long bidderId = callbackQuery.getFrom().getId();
        String bidderDisplayName = BidderDisplayName.from(callbackQuery.getFrom());

        BidService.BidResult result = bidService.increase(adId, bidderId, bidderDisplayName);
        return switch (result) {
            case ACCEPTED -> answer(callbackQuery, "Ставка принята");
            case NOT_FOUND -> answer(callbackQuery, "Лот не найден или уже недоступен");
            case REJECTED -> answer(callbackQuery, "Ставка не принята: аукцион завершен или ставка недоступна");
        };
    }

    private AnswerCallbackQuery answer(CallbackQuery callbackQuery, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());
        answer.setText(text);
        answer.setShowAlert(false);
        return answer;
    }
}
