package smartbid.tg.domain;

import smartbid.tg.domain.LotDraft;

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
