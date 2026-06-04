package smartbid.tg.telegram.router;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import smartbid.tg.telegram.handler.callback.CallbackHandler;
import smartbid.tg.telegram.handler.command.CommandHandler;

import java.util.List;
import java.util.Optional;

@Component
public class UpdateRouter {

    private final List<CommandHandler> commandHandlers;
    private final List<CallbackHandler> callbackHandlers;

    public UpdateRouter(List<CommandHandler> commandHandlers, List<CallbackHandler> callbackHandlers) {
        this.commandHandlers = commandHandlers;
        this.callbackHandlers = callbackHandlers;
    }

    public List<BotApiMethod<?>> route(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return routeCommand(update);
        }

        if (update.hasCallbackQuery()) {
            return routeCallback(update);
        }

        return List.of();
    }

    private List<BotApiMethod<?>> routeCommand(Update update) {
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
}
