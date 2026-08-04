import os
import time
import base64
import uuid
import httpx
from typing import Optional
from fastapi import FastAPI, UploadFile, File, Form, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import uvicorn
from openrouter_service import generate_try_on_image, RateLimitExceeded
from config import UPLOAD_DIR_PERSON, UPLOAD_DIR_TOPS, UPLOAD_DIR_BOTTOMS, OUTPUT_DIR

# Ensure directories exist
for directory in [UPLOAD_DIR_PERSON, UPLOAD_DIR_TOPS, UPLOAD_DIR_BOTTOMS, OUTPUT_DIR]:
    os.makedirs(directory, exist_ok=True)

app = FastAPI(title="ÉLAN Personal AI Wardrobe API")

# Enable CORS for all clients/origins
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Serve generated outputs statically
app.mount("/outputs", StaticFiles(directory="outputs"), name="outputs")

def encode_image_to_base64(file_bytes: bytes) -> str:
    return base64.b64encode(file_bytes).decode("utf-8")

def save_upload(upload_dir: str, prefix: str, file_bytes: bytes) -> str:
    file_id = str(uuid.uuid4())[:8]
    date_str = time.strftime("%Y%m%d")
    filename = f"{prefix}_{date_str}_{file_id}.jpg"
    filepath = os.path.join(upload_dir, filename)
    with open(filepath, "wb") as f:
        f.write(file_bytes)
    return filepath

def save_generated_image(image_data: str, request: Request) -> str:
    file_id = str(uuid.uuid4())[:8]
    date_str = time.strftime("%Y%m%d")
    filename = f"generated_{date_str}_{file_id}.png"
    filepath = os.path.join(OUTPUT_DIR, filename)
        
    if image_data.startswith("data:image"):
        # It's a base64 data URI
        header, encoded = image_data.split(",", 1)
        with open(filepath, "wb") as f:
            f.write(base64.b64decode(encoded))
        base_url = str(request.base_url).rstrip("/")
        return f"{base_url}/outputs/generated/{filename}"
    elif image_data.startswith("http"):
        return image_data
                
    return image_data

@app.get("/")
def read_root():
    return {"status": "online", "app": "ÉLAN Backend API"}

@app.post("/try-on")
async def generate_try_on(
    request: Request,
    person_image: UploadFile = File(...),
    top_image: Optional[UploadFile] = File(None),
    bottom_image: Optional[UploadFile] = File(None)
):
    """
    Virtual Try-On & Outfit Styling API endpoint.
    """
    try:
        person_bytes = await person_image.read()
        # save_upload(UPLOAD_DIR_PERSON, "person", person_bytes)
        person_b64 = encode_image_to_base64(person_bytes)
        
        top_b64 = None
        if top_image:
            top_bytes = await top_image.read()
            # save_upload(UPLOAD_DIR_TOPS, "top", top_bytes)
            top_b64 = encode_image_to_base64(top_bytes)
            
        bottom_b64 = None
        if bottom_image:
            bottom_bytes = await bottom_image.read()
            # save_upload(UPLOAD_DIR_BOTTOMS, "bottom", bottom_bytes)
            bottom_b64 = encode_image_to_base64(bottom_bytes)
            
        if not top_b64 and not bottom_b64:
            return JSONResponse(status_code=400, content={"error": "Must provide at least a top_image or bottom_image."})

        generated_image_raw, model_id = await generate_try_on_image(person_b64, top_b64, bottom_b64)
        
        if generated_image_raw:
            final_url = save_generated_image(generated_image_raw, request)
            return JSONResponse(status_code=200, content={
                "status": "success",
                "limit_exceeded": False,
                "message": f"Try-on composition created via OpenRouter",
                "output_url": final_url,
                "model_used": model_id,
                "timestamp": int(time.time())
            })
        else:
            return JSONResponse(status_code=502, content={
                "status": "error",
                "limit_exceeded": False,
                "message": "Failed to generate image. Models returned no image output."
            })
            
    except RateLimitExceeded as rle:
        return JSONResponse(status_code=429, content={
            "status": "limit_exceeded",
            "limit_exceeded": True,
            "message": str(rle)
        })
    except Exception as e:
        import traceback
        traceback.print_exc()
        return JSONResponse(status_code=500, content={
            "status": "error",
            "limit_exceeded": False,
            "message": f"Server error: {str(e)}"
        })

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
