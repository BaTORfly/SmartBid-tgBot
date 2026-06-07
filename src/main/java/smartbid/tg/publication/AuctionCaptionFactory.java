package smartbid.tg.publication;

import org.springframework.stereotype.Component;
import smartbid.tg.backend.BackendAd;
import smartbid.tg.common.MoneyFormatter;
import smartbid.tg.kafka.AdFinishedEvent;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class AuctionCaptionFactory {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String active(BackendAd ad) {
        return """
                %s

                %s

                Текущая цена: %s р
                Текущий участник: пока нет
                Завершение: %s
                """.formatted(
                ad.title(),
                description(ad.description()),
                MoneyFormatter.rublesFromKopecks(ad.price()),
                formatEndTime(ad.expiresAt())
        );
    }

    public String finished(AdFinishedEvent event) {
        String statusText = switch (event.status()) {
            case "bought" -> "Аукцион завершен. Победитель: %s.".formatted(winner(event.pretendentId()));
            case "expired" -> "Аукцион завершен без ставок.";
            case "removed" -> "Лот снят с публикации.";
            default -> "Аукцион завершен со статусом: " + event.status();
        };

        return """
                %s

                Финальная цена: %s р
                """.formatted(statusText, MoneyFormatter.rublesFromKopecks(event.finalPrice()));
    }

    private String description(String description) {
        if (description == null || description.isBlank()) {
            return "Без описания";
        }
        return description;
    }

    private String formatEndTime(OffsetDateTime expiresAt) {
        if (expiresAt == null) {
            return "уточняется";
        }
        return expiresAt.atZoneSameInstant(MOSCOW_ZONE).format(DATE_TIME_FORMATTER) + " МСК";
    }

    private String winner(Long pretendentId) {
        if (pretendentId == null) {
            return "не определен";
        }
        return "Telegram ID " + pretendentId;
    }
}
