# SmartBid Telegram Dispatcher

Dispatcher-service between Telegram bot, Telegram community, and SmartBid backend.

Current stage: lot draft collection, backend evaluation, publication confirmation, Kafka-driven community posting, and auction finish updates. The bot works through Long Polling, while Kafka consumers handle backend events for published and finished ads.

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
| `TELEGRAM_TARGET_CHAT_ID` | no | `-1003999451875` | Target Telegram community chat id. |
| `BACKEND_URL` | no | `http://localhost:8080` | SmartBid backend URL. |
| `KAFKA_BOOTSTRAP_SERVERS` | no | `localhost:9092` | Kafka bootstrap servers. Use `kafka:9092` inside backend Docker network. |
| `KAFKA_AD_CREATED_TOPIC` | no | `ad-created` | Topic with backend publication events. |
| `KAFKA_AD_FINISHED_TOPIC` | no | `ad-finished` | Topic with backend auction finish events. |
| `KAFKA_CONSUMER_GROUP_ID` | no | `smartbid-tg` | Kafka consumer group id. |
| `KAFKA_CONSUMER_ENABLED` | no | `true` | Enables Kafka consumers. |
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
- the bot shows the returned lot id, initial price, and publication actions;
- after publication confirmation, backend emits `ad-created`, and the bot publishes the lot in the target Telegram chat;
- when backend emits `ad-finished`, the bot edits the Telegram community post.

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
KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092
KAFKA_AD_CREATED_TOPIC=ad-created
KAFKA_AD_FINISHED_TOPIC=ad-finished
KAFKA_CONSUMER_GROUP_ID=smartbid-tg
KAFKA_CONSUMER_ENABLED=true
TELEGRAM_TARGET_CHAT_ID=-1003999451875
```

Run the container with this env file:

```powershell
docker run --rm `
  --name smartbid-tg-app `
  --network smartbid-backend_default `
  --env-file .\.env `
  -e BACKEND_URL=http://smartbid-backend:8080 `
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 `
  -p 8000:8000 `
  smartbid-tg
```

`application.yaml` is already inside the image and provides defaults for optional settings. The token is intentionally not stored there, so Docker needs either `--env-file .env` or explicit `-e TELEGRAM_BOT_TOKEN=...`.

When backend runs on the host machine and the bot runs in Docker, use `BACKEND_URL=http://host.docker.internal:8080`.

When backend runs through its Docker Compose and the bot runs as a separate container, run the bot in the backend Compose network and use:

```env
BACKEND_URL=http://smartbid-backend:8080
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

If Docker Desktop is paused, unpause it before running `docker build` or `docker run`.
