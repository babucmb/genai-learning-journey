# OpenAI Telegram Bot

A small Generative AI learning project that connects a Telegram bot to the OpenAI API. Send the bot a text message in Telegram and it returns an AI-generated reply.

## What this project teaches

- Reading secrets from environment variables
- Creating a Telegram bot with `aiogram`
- Calling an LLM with the OpenAI Python SDK
- Handling simple Telegram commands and text messages
- Organizing an application into configuration, bot handlers, and LLM code

## Project structure

```text
openai_bot/
├── main.py           # Starts Telegram polling
├── bot.py            # Telegram handlers
├── llm.py            # OpenAI API call
├── config.py         # Loads environment variables
├── requirements.txt  # Python dependencies
└── .env              # Local secrets (do not commit)
```

## Prerequisites

- Python 3.10 or later
- A Telegram bot token from [@BotFather](https://t.me/BotFather)
- An OpenAI API key and available API credits

> A ChatGPT subscription and OpenAI API billing are separate. Add API credits or configure billing in the OpenAI Platform before using the bot.

## Setup

1. Clone the repository and enter the project directory.

2. Create and activate a virtual environment on Windows PowerShell:

   ```powershell
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   ```

3. Install dependencies:

   ```powershell
   python -m pip install -r requirements.txt
   ```

4. Create a `.env` file in the project root:

   ```env
   TELEGRAM_BOT_TOKEN="your_telegram_bot_token"
   OPENAI_API_KEY="your_openai_api_key"
   OPENAI_MODEL="gpt-4o-mini"
   ```

5. Start the bot:

   ```powershell
   python main.py
   ```

6. Open Telegram, send `/start` to your bot, then send a text message.

## How it works

1. `main.py` creates the Telegram bot and dispatcher.
2. `bot.py` registers message handlers.
3. Each text message is passed to `ask_openai()` in `llm.py`.
4. `llm.py` sends the prompt to the configured OpenAI model.
5. The generated response is sent back to Telegram.

## Common issues

### `429 Too Many Requests`

This can mean a temporary API rate limit, no remaining API credit, or a project/organization spend limit. Check your OpenAI Platform billing, credits, and usage limits. Retrying will not fix an exhausted credit balance.

### `ModuleNotFoundError`

Activate `.venv` and install the dependencies again:

```powershell
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

### Bot does not reply

Confirm that both variables in `.env` are present and valid, then restart the program. Do not share either token in chat, screenshots, or commits.


## Next learning steps

- Add conversation memory per Telegram user
- Use an async OpenAI client so API calls do not block the bot
- Add streaming responses
- Add command handlers such as `/help` and `/clear`
- Improve user-friendly error messages and logging

## Documentation

- [OpenAI API quickstart](https://platform.openai.com/docs/quickstart)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [aiogram documentation](https://docs.aiogram.dev/)
