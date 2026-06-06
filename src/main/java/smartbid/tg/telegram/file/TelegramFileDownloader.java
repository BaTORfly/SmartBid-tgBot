package smartbid.tg.telegram.file;

public interface TelegramFileDownloader {

    byte[] download(String fileId);
}
