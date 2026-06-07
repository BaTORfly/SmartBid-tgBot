package smartbid.tg.infrastructure.telegram.handler.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import smartbid.tg.domain.ConversationState;
import smartbid.tg.domain.ConversationStep;
import smartbid.tg.domain.ConversationStorage;
import smartbid.tg.domain.LotDraft;
import smartbid.tg.domain.PendingLotPublicationStorage;
import smartbid.tg.infrastructure.telegram.callback.CallbackData;

@Component
public class RetryLotCallbackHandler implements CallbackHandler {

    private final ConversationStorage conversationStorage;
    private final PendingLotPublicationStorage pendingPublicationStorage;

    public RetryLotCallbackHandler(
            ConversationStorage conversationStorage,
            PendingLotPublicationStorage pendingPublicationStorage
    ) {
        this.conversationStorage = conversationStorage;
        this.pendingPublicationStorage = pendingPublicationStorage;
    }

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return CallbackData.RETRY_LOT.equals(callbackQuery.getData());
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        Long telegramUserId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();

        pendingPublicationStorage.deleteByOwnerUserId(telegramUserId);
        conversationStorage.save(new ConversationState(
                ConversationStep.WAITING_TITLE,
                LotDraft.empty(telegramUserId, chatId)
        ));

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText("Напиши название лота.");
        return response;
    }
}
