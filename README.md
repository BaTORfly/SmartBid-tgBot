# SmartBid Telegram Dispatcher

Dispatcher-service between Telegram bot, Telegram community, and SmartBid backend.

Current stage: Telegram bot startup and lot draft collection. The bot works through Long Polling, collects lot title, description, and one photo, then sends the lot to backend for initial price calculation.

## Requirements

- Java 17
- Maven 3.9+ or project Maven Wrapper
- Docker, optional
- Telegram bot token from BotFather

## Environment Variables

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `TELEGRAM_BOT_TOKEN` | yes | empty | Telegram bot token. |
| `TELEGRAM_BOT_USERNAME` | no | `reu_smart_bit_bot` | Telegram bot username. |
| `TELEGRAM_BOT_ENABLED` | no | `true` | Enables bot registration in Telegram Long Polling. |
| `TELEGRAM_TARGET_CHAT_ID` | no | `-1003999451875` | Target Telegram community chat id. Not used at the current stage. |
| `BACKEND_URL` | no | `http://localhost:8080` | SmartBid backend URL. |
| `SERVER_PORT` | no | `8000` | Spring Boot HTTP port. |

Do not store a real Telegram token in `application.yaml`.

## Local Run

PowerShell:

```powershell
$env:TELEGRAM_BOT_TOKEN="your-bot-token"
$env:TELEGRAM_BOT_USERNAME="your_bot_username"
mvn spring-boot:run
```

After startup, send `/start` to the bot in Telegram.

Expected behavior:

- the bot sends a greeting;
- the bot shows an inline button for offering a lot for publication;
- pressing the button starts lot draft collection;
- the bot asks for title, description, and one photo;
- after photo upload, the bot downloads the Telegram file and sends the lot to backend;
- the bot shows the returned lot id and initial price.

## Tests

```powershell
mvn test
```

## Build Jar

```powershell
mvn package
```

The executable jar will be created in `target/`.

## Docker Build

```powershell
docker build -t smartbid-tg .
```

## Docker Run

Create a non-committed `.env` file in the project root:

```env
TELEGRAM_BOT_TOKEN=your-bot-token
TELEGRAM_BOT_USERNAME=your_bot_username
SERVER_PORT=8000
BACKEND_URL=http://host.docker.internal:8080
```

Run the container with this env file:

```powershell
docker run --rm `
  --name smartbid-tg-app `
  --env-file .env `
  -p 8000:8000 `
  smartbid-tg
```

`application.yaml` is already inside the image and provides defaults for optional settings. The token is intentionally not stored there, so Docker needs either `--env-file .env` or explicit `-e TELEGRAM_BOT_TOKEN=...`.

When backend runs on the host machine and the bot runs in Docker, use `BACKEND_URL=http://host.docker.internal:8080`.

If Docker Desktop is paused, unpause it before running `docker build` or `docker run`.
