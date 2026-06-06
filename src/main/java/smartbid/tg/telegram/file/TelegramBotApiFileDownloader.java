package smartbid.tg.telegram.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import smartbid.tg.config.TelegramBotProperties;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramBotApiFileDownloader implements TelegramFileDownloader {

    private final RestClient restClient;
    private final TelegramBotProperties properties;

    public TelegramBotApiFileDownloader(RestClient.Builder restClientBuilder, TelegramBotProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    @Override
    public byte[] download(String fileId) {
        TelegramGetFileResponse fileResponse = restClient.get()
                .uri(URI.create("https://api.telegram.org/bot" + properties.token()
                        + "/getFile?file_id=" + URLEncoder.encode(fileId, StandardCharsets.UTF_8)))
                .retrieve()
                .body(TelegramGetFileResponse.class);

        if (fileResponse == null || fileResponse.result() == null || fileResponse.result().filePath() == null) {
            throw new IllegalStateException("Telegram returned empty file path");
        }

        byte[] file = restClient.get()
                .uri(URI.create("https://api.telegram.org/file/bot" + properties.token() + "/" + fileResponse.result().filePath()))
                .retrieve()
                .body(byte[].class);

        if (file == null || file.length == 0) {
            throw new IllegalStateException("Telegram returned empty file");
        }

        return file;
    }

    private record TelegramGetFileResponse(
            boolean ok,
            TelegramFile result
    ) {
    }

    private record TelegramFile(
            @JsonProperty("file_id") String fileId,
            @JsonProperty("file_path") String filePath
    ) {
    }
}
