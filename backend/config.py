import os
from dotenv import load_dotenv

load_dotenv()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
if not OPENROUTER_API_KEY:
    raise ValueError("OPENROUTER_API_KEY is not set in the environment.")

OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"

PREFERRED_MODELS = [
    "google/gemini-2.5-flash-image",
    "bytedance-seed/seedream-4.5"
]

UPLOAD_DIR_PERSON = "uploads/person"
UPLOAD_DIR_TOPS = "uploads/tops"
UPLOAD_DIR_BOTTOMS = "uploads/bottoms"
OUTPUT_DIR = "outputs/generated"
