package smartbid.tg.telegram.handler.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import smartbid.tg.backend.BackendLotClient;
import smartbid.tg.publication.PendingLotPublication;
import smartbid.tg.publication.PendingLotPublicationStorage;
import smartbid.tg.telegram.callback.CallbackData;

@Slf4j
@Component
public class PublishLotCallbackHandler implements CallbackHandler {

    private final PendingLotPublicationStorage pendingPublicationStorage;
    private final BackendLotClient backendLotClient;

    public PublishLotCallbackHandler(
            PendingLotPublicationStorage pendingPublicationStorage,
            BackendLotClient backendLotClient
    ) {
        this.pendingPublicationStorage = pendingPublicationStorage;
        this.backendLotClient = backendLotClient;
    }

    @Override
    public boolean supports(CallbackQuery callbackQuery) {
        return CallbackData.isPublishLot(callbackQuery.getData());
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        String adId = CallbackData.publishLotAdId(callbackQuery.getData());
        Long chatId = callbackQuery.getMessage().getChatId();

        return pendingPublicationStorage.findByAdId(adId)
                .map(publication -> publish(publication, chatId))
                .orElseGet(() -> send(chatId, "Не нашел лот для публикации. Начни заново через /start."));
    }

    private SendMessage publish(PendingLotPublication publication, Long chatId) {
        try {
            backendLotClient.publishLot(publication.adId(), publication.ownerChatId());
            pendingPublicationStorage.deleteByAdId(publication.adId());
            return send(chatId, "Лот принят к публикации. Скоро он появится в сообществе.");
        } catch (RuntimeException exception) {
            log.warn("Failed to publish lot {} in backend: {}", publication.adId(), exception.getMessage());
            return send(chatId, "Не получилось отправить лот на публикацию. Попробуй еще раз чуть позже.");
        }
    }

    private SendMessage send(Long chatId, String text) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(text);
        return response;
    }
}
