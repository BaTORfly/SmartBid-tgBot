package smartbid.tg.telegram.handler.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import smartbid.tg.conversation.ConversationState;
import smartbid.tg.conversation.ConversationStep;
import smartbid.tg.conversation.ConversationStorage;
import smartbid.tg.lot.LotDraft;
import smartbid.tg.publication.PendingLotPublicationStorage;
import smartbid.tg.telegram.callback.CallbackData;

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
