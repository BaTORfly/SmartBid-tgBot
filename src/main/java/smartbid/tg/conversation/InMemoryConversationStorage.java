package smartbid.tg.conversation;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryConversationStorage implements ConversationStorage {

    private final ConcurrentMap<Long, ConversationState> states = new ConcurrentHashMap<>();

    @Override
    public Optional<ConversationState> findByUserId(Long telegramUserId) {
        return Optional.ofNullable(states.get(telegramUserId));
    }

    @Override
    public void save(ConversationState state) {
        states.put(state.draft().telegramUserId(), state);
    }

    @Override
    public void deleteByUserId(Long telegramUserId) {
        states.remove(telegramUserId);
    }
}
