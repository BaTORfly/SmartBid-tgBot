package smartbid.tg.infrastructure.telegram.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import smartbid.tg.infrastructure.backend.BackendLot;
import smartbid.tg.infrastructure.backend.BackendLotClient;
import smartbid.tg.infrastructure.backend.LotSubmission;
import smartbid.tg.shared.MoneyFormatter;
import smartbid.tg.domain.ConversationState;
import smartbid.tg.domain.ConversationStep;
import smartbid.tg.domain.ConversationStorage;
import smartbid.tg.domain.LotDraft;
import smartbid.tg.domain.PendingLotPublication;
import smartbid.tg.domain.PendingLotPublicationStorage;
import smartbid.tg.infrastructure.telegram.file.TelegramFileDownloader;
import smartbid.tg.infrastructure.telegram.keyboard.PostEvaluationKeyboardFactory;

import java.util.Comparator;

@Slf4j
@Component
public class LotDraftMessageHandler implements MessageHandler {

    private final ConversationStorage conversationStorage;
    private final TelegramFileDownloader fileDownloader;
    private final BackendLotClient backendLotClient;
    private final PendingLotPublicationStorage pendingPublicationStorage;
    private final PostEvaluationKeyboardFactory postEvaluationKeyboardFactory;

    public LotDraftMessageHandler(
            ConversationStorage conversationStorage,
            TelegramFileDownloader fileDownloader,
            BackendLotClient backendLotClient,
            PendingLotPublicationStorage pendingPublicationStorage,
            PostEvaluationKeyboardFactory postEvaluationKeyboardFactory
    ) {
        this.conversationStorage = conversationStorage;
        this.fileDownloader = fileDownloader;
        this.backendLotClient = backendLotClient;
        this.pendingPublicationStorage = pendingPublicationStorage;
        this.postEvaluationKeyboardFactory = postEvaluationKeyboardFactory;
    }

    @Override
    public boolean supports(Message message) {
        return conversationStorage.findByUserId(message.getFrom().getId()).isPresent();
    }

    @Override
    public BotApiMethod<?> handle(Message message) {
        ConversationState state = conversationStorage.findByUserId(message.getFrom().getId())
                .orElseThrow(() -> new IllegalStateException("Conversation state is required"));

        return switch (state.step()) {
            case WAITING_TITLE -> handleTitle(message, state);
            case WAITING_DESCRIPTION -> handleDescription(message, state);
            case WAITING_PHOTO -> handlePhoto(message, state);
        };
    }

    private SendMessage handleTitle(Message message, ConversationState state) {
        if (!message.hasText() || message.getText().isBlank()) {
            return send(message, "Пожалуйста, напиши название лота текстом.");
        }

        LotDraft updatedDraft = state.draft().withTitle(message.getText().trim());
        conversationStorage.save(new ConversationState(ConversationStep.WAITING_DESCRIPTION, updatedDraft));
        return send(message, "Отлично. Теперь напиши описание лота.");
    }

    private SendMessage handleDescription(Message message, ConversationState state) {
        if (!message.hasText() || message.getText().isBlank()) {
            return send(message, "Пожалуйста, напиши описание лота текстом.");
        }

        LotDraft updatedDraft = state.draft().withDescription(message.getText().trim());
        conversationStorage.save(new ConversationState(ConversationStep.WAITING_PHOTO, updatedDraft));
        return send(message, "Хорошо. Теперь загрузи одно фото лота.");
    }

    private SendMessage handlePhoto(Message message, ConversationState state) {
        if (!message.hasPhoto()) {
            return send(message, "Пожалуйста, загрузи одно фото лота.");
        }

        String photoFileId = message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElseThrow(() -> new IllegalStateException("Photo message has no photo sizes"));

        LotDraft completedDraft = state.draft().withPhotoFileId(photoFileId);

        try {
            byte[] photo = fileDownloader.download(photoFileId);
            BackendLot backendLot = backendLotClient.createLot(new LotSubmission(completedDraft, photo, message.getMessageId()));
            conversationStorage.deleteByUserId(message.getFrom().getId());
            pendingPublicationStorage.save(new PendingLotPublication(
                    backendLot.id(),
                    completedDraft.chatId(),
                    completedDraft.telegramUserId(),
                    completedDraft.title(),
                    completedDraft.description(),
                    completedDraft.photoFileId(),
                    backendLot.price()
            ));
            SendMessage response = send(message, summary(completedDraft, backendLot));
            response.setReplyMarkup(postEvaluationKeyboardFactory.create(backendLot.id()));
            return response;
        } catch (RuntimeException exception) {
            log.warn("Failed to send lot draft to backend: {}", exception.getMessage());
            return send(message, "Не получилось отправить лот на оценку. Попробуй загрузить фото еще раз чуть позже.");
        }
    }

    private SendMessage send(Message message, String text) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText(text);
        return response;
    }

    private String summary(LotDraft draft, BackendLot backendLot) {
        return """
                Лот отправлен на оценку.

                ID лота: %s
                Название: %s
                Описание: %s
                Начальная цена: %s р

                На следующем этапе добавим подтверждение публикации.
                """.formatted(
                backendLot.id(),
                draft.title(),
                draft.description(),
                MoneyFormatter.rublesFromKopecks(backendLot.price())
        );
    }
}
