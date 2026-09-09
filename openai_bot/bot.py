# Bot Code
from aiogram import Dispatcher, F, Router
from aiogram.filters import CommandStart
from aiogram.types import Message

from llm import ask_openai

router = Router()


@router.message(CommandStart())
async def start_command(message: Message) -> None:
    await message.answer("Hello! Send me a message and I will assist with you.")


@router.message(F.text)
async def chat(message: Message) -> None:
    if not message.text:
        return

    try:
        answer = ask_openai(message.text)
        await message.answer(answer or "I could not generate a response.")
    except Exception:
        await message.answer("Sorry, I could not process that message right now.")


def register_handlers(dispatcher: Dispatcher) -> None:
    """Attach this module's Telegram routes to the application dispatcher."""
    dispatcher.include_router(router)
