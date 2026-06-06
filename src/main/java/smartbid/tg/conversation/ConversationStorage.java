package smartbid.tg.conversation;

import java.util.Optional;

public interface ConversationStorage {

    Optional<ConversationState> findByUserId(Long telegramUserId);

    void save(ConversationState state);

    void deleteByUserId(Long telegramUserId);
}
