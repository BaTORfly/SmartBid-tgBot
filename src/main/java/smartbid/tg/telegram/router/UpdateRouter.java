package smartbid.tg.telegram.router;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import smartbid.tg.telegram.handler.callback.CallbackHandler;
import smartbid.tg.telegram.handler.command.CommandHandler;
import smartbid.tg.telegram.handler.message.MessageHandler;

import java.util.List;
import java.util.Optional;

@Component
public class UpdateRouter {

    private final List<CommandHandler> commandHandlers;
    private final List<CallbackHandler> callbackHandlers;
    private final List<MessageHandler> messageHandlers;

    public UpdateRouter(
            List<CommandHandler> commandHandlers,
            List<CallbackHandler> callbackHandlers,
            List<MessageHandler> messageHandlers
    ) {
        this.commandHandlers = commandHandlers;
        this.callbackHandlers = callbackHandlers;
        this.messageHandlers = messageHandlers;
    }

    public List<BotApiMethod<?>> route(Update update) {
        if (update.hasMessage()) {
            List<BotApiMethod<?>> commandResponses = routeCommand(update);
            if (!commandResponses.isEmpty()) {
                return commandResponses;
            }
            return routeMessage(update);
        }

        if (update.hasCallbackQuery()) {
            return routeCallback(update);
        }

        return List.of();
    }

    private List<BotApiMethod<?>> routeCommand(Update update) {
        if (!update.getMessage().hasText()) {
            return List.of();
        }

        return commandHandlers.stream()
                .filter(handler -> handler.supports(update.getMessage()))
                .findFirst()
                .map(handler -> List.<BotApiMethod<?>>of(handler.handle(update.getMessage())))
                .orElseGet(List::of);
    }

    private List<BotApiMethod<?>> routeCallback(Update update) {
        Optional<BotApiMethod<?>> response = callbackHandlers.stream()
                .filter(handler -> handler.supports(update.getCallbackQuery()))
                .findFirst()
                .map(handler -> handler.handle(update.getCallbackQuery()));

        if (response.isPresent()) {
            return List.of(response.get());
        }
        return List.of();
    }

    private List<BotApiMethod<?>> routeMessage(Update update) {
        return messageHandlers.stream()
                .filter(handler -> handler.supports(update.getMessage()))
                .findFirst()
                .map(handler -> List.<BotApiMethod<?>>of(handler.handle(update.getMessage())))
                .orElseGet(List::of);
    }
}
