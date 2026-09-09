# main file to asscces the file in a singel flow
import asyncio
import logging
from aiogram import Bot, Dispatcher
from config import TELEGRAM_BOT_TOKEN
from bot import register_handlers

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)

async def main():
    bot = Bot(token=TELEGRAM_BOT_TOKEN)
    dp = Dispatcher()

    register_handlers(dp)

    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main())
