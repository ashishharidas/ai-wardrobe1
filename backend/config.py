import os
from dotenv import load_dotenv

load_dotenv()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
if not OPENROUTER_API_KEY:
    raise ValueError("OPENROUTER_API_KEY is not set in the environment.")

OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"

UPLOAD_DIR_FAVORITES = "outputs/favorites"
<<<<<<< HEAD
PREFERRED_MODELS = [
    "google/gemini-2.5-flash-image",
    "bytedance-seed/seedream-4.5",
    "anthropic/claude-3.7-sonnet",
    "openai/gptimage",
    "alireza1197/flux-krea-dev"
=======

# Only image-generation-capable models belong here. Removed:
#   - "anthropic/claude-3.7-sonnet": text-only reasoning model, cannot output images at all.
#   - "openai/gptimage": not a valid OpenRouter slug format (should be something like
#     "openai/gpt-image-1" if you want this provider — verify the exact current slug
#     on OpenRouter's model listing before adding it back).
#   - "alireza1197/flux-krea-dev": "alireza1197/" isn't an OpenRouter provider namespace;
#     this looks like a Hugging Face username, not a valid OpenRouter model ID. If you
#     want a FLUX Krea model, look up the real OpenRouter slug (likely under
#     "black-forest-labs/" instead) before using it.
PREFERRED_MODELS = [
    "google/gemini-2.5-flash-image",
    "bytedance-seed/seedream-4.5",
>>>>>>> c6137af3a64f7724067108f89162e91993b11c73
]

UPLOAD_DIR_PERSON = "uploads/person"
UPLOAD_DIR_TOPS = "uploads/tops"
UPLOAD_DIR_BOTTOMS = "uploads/bottoms"
OUTPUT_DIR = "outputs/generated"