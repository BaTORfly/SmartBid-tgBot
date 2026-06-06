package smartbid.tg.conversation;

import smartbid.tg.lot.LotDraft;

public record ConversationState(
        ConversationStep step,
        LotDraft draft
) {

    public ConversationState withStep(ConversationStep step) {
        return new ConversationState(step, draft);
    }

    public ConversationState withDraft(LotDraft draft) {
        return new ConversationState(step, draft);
    }
}
