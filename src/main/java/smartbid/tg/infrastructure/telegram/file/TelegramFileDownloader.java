package smartbid.tg.infrastructure.telegram.file;

public interface TelegramFileDownloader {

    byte[] download(String fileId);
}
