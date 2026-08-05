import os
import time
import base64
import uuid
import httpx
import json
from typing import Optional
from fastapi import FastAPI, UploadFile, File, Form, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import uvicorn
from openrouter_service import generate_try_on_image, RateLimitExceeded
from config import UPLOAD_DIR_PERSON, UPLOAD_DIR_TOPS, UPLOAD_DIR_BOTTOMS, OUTPUT_DIR, UPLOAD_DIR_FAVORITES

# Ensure directories exist
for directory in [UPLOAD_DIR_PERSON, UPLOAD_DIR_TOPS, UPLOAD_DIR_BOTTOMS, OUTPUT_DIR, UPLOAD_DIR_FAVORITES]:
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

def get_image_hash(image_bytes: bytes) -> str:
    """Generate a SHA256 hash for an image to identify unique images."""
    import hashlib
    return hashlib.sha256(image_bytes).hexdigest()[:16]

def get_combo_hash(person_bytes: Optional[bytes], top_bytes: Optional[bytes], bottom_bytes: Optional[bytes]) -> str:
    """Generate a unique hash for a person+top+bottom image combination."""
    import hashlib
    hasher = hashlib.sha256()
    if person_bytes:
        hasher.update(person_bytes)
    if top_bytes:
        hasher.update(top_bytes)
    if bottom_bytes:
        hasher.update(bottom_bytes)
    return hasher.hexdigest()[:16]

def save_upload(upload_dir: str, prefix: str, file_bytes: bytes) -> str:
    file_id = str(uuid.uuid4())[:8]
    date_str = time.strftime("%Y%m%d")
    filename = f"{prefix}_{date_str}_{file_id}.jpg"
    filepath = os.path.join(upload_dir, filename)
    with open(filepath, "wb") as f:
        f.write(file_bytes)
    return filepath

def save_generated_image(image_data, request: Request, combo_hash: str = None) -> str:
    # Handle case where OpenRouter returns image data as a dict instead of a string
    if isinstance(image_data, dict):
        # OpenRouter may return {"url": "..."} or {"base64": "..."}
        image_data = image_data.get("url") or image_data.get("base64") or str(image_data)

    # Use combo_hash as filename if provided, otherwise generate UUID
    if combo_hash:
        filename = f"{combo_hash}.png"
    else:
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
        # Download the image and save it locally
        import httpx as async_httpx
        with async_httpx.Client() as client:
            response = client.get(image_data)
            if response.status_code == 200:
                with open(filepath, "wb") as f:
                    f.write(response.content)
        base_url = str(request.base_url).rstrip("/")
        return f"{base_url}/outputs/generated/{filename}"

    return image_data

def check_existing_generation(combo_hash: str) -> Optional[str]:
    """Check if an image for this combo already exists in generated folder."""
    filename = f"{combo_hash}.png"
    filepath = os.path.join(OUTPUT_DIR, filename)
    if os.path.exists(filepath):
        return filename
    return None

def add_to_favorites(source_url: str, request: Request) -> str:
    """Copy a generated image to the favorites folder."""
    # Parse the source path from URL
    base_url = str(request.base_url).rstrip("/")
    if source_url.startswith(base_url):
        source_path = source_url.replace(base_url + "/outputs/", "outputs/")
    else:
        return None

    if not os.path.exists(source_path):
        return None

    filename = os.path.basename(source_path)
    dest_path = os.path.join(UPLOAD_DIR_FAVORITES, filename)
    with open(source_path, "rb") as f:
        content = f.read()
    with open(dest_path, "wb") as f:
        f.write(content)

    return f"{base_url}/outputs/favorites/{filename}"

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
    Checks for existing generation before creating new ones.
    """
    try:
        person_bytes = await person_image.read()
        person_b64 = encode_image_to_base64(person_bytes)

        top_b64 = None
        top_bytes = None
        if top_image:
            top_bytes = await top_image.read()
            top_b64 = encode_image_to_base64(top_bytes)

        bottom_b64 = None
        bottom_bytes = None
        if bottom_image:
            bottom_bytes = await bottom_image.read()
            bottom_b64 = encode_image_to_base64(bottom_bytes)

        if not top_b64 and not bottom_b64:
            return JSONResponse(status_code=400, content={"error": "Must provide at least a top_image or bottom_image."})

        # Generate combo hash to check for existing generation
        combo_hash = get_combo_hash(person_bytes, top_bytes, bottom_bytes)

        # Check if we already have a generated image for this combo
        existing_filename = check_existing_generation(combo_hash)
        if existing_filename:
            base_url = str(request.base_url).rstrip("/")
            existing_url = f"{base_url}/outputs/generated/{existing_filename}"
            return JSONResponse(status_code=200, content={
                "status": "success",
                "limit_exceeded": False,
                "message": f"Try-on image already generated",
                "output_url": existing_url,
                "model_used": "cached",
                "timestamp": int(time.time())
            })

        # Generate new image
        generated_image_raw, model_id = await generate_try_on_image(person_b64, top_b64, bottom_b64)

        if generated_image_raw:
            final_url = save_generated_image(generated_image_raw, request, combo_hash=combo_hash)
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

@app.post("/favorites/add")
async def add_favorite(request: Request, image_url: str = Form(...)):
    """Add a generated image to favorites."""
    try:
        favorite_url = add_to_favorites(image_url, request)
        if favorite_url:
            return JSONResponse(status_code=200, content={
                "status": "success",
                "message": "Image added to favorites",
                "favorite_url": favorite_url
            })
        else:
            return JSONResponse(status_code=404, content={
                "status": "error",
                "message": "Image not found or invalid URL"
            })
    except Exception as e:
        return JSONResponse(status_code=500, content={
            "status": "error",
            "message": f"Server error: {str(e)}"
        })

@app.get("/favorites/list")
async def list_favorites(request: Request):
    """List all favorite images."""
    try:
        base_url = str(request.base_url).rstrip("/")
        favorites_dir = os.path.join("outputs", "favorites")
        if os.path.exists(favorites_dir):
            files = [f for f in os.listdir(favorites_dir) if f.endswith(('.png', '.jpg', '.jpeg'))]
            favorites = [f"{base_url}/outputs/favorites/{f}" for f in files]
        else:
            favorites = []
        return JSONResponse(status_code=200, content={
            "status": "success",
            "favorites": favorites
        })
    except Exception as e:
        return JSONResponse(status_code=500, content={
            "status": "error",
            "message": f"Server error: {str(e)}"
        })

@app.delete("/favorites/remove")
async def remove_favorite(image_url: str = Form(...)):
    """Remove an image from favorites."""
    try:
        # Parse the source path from URL
        filename = os.path.basename(image_url)
        filepath = os.path.join("outputs", "favorites", filename)
        if os.path.exists(filepath):
            os.remove(filepath)
            return JSONResponse(status_code=200, content={
                "status": "success",
                "message": "Image removed from favorites"
            })
        else:
            return JSONResponse(status_code=404, content={
                "status": "error",
                "message": "Image not found in favorites"
            })
    except Exception as e:
        return JSONResponse(status_code=500, content={
            "status": "error",
            "message": f"Server error: {str(e)}"
        })

@app.get("/generated/list")
async def list_generated(request: Request):
    """List all generated images."""
    try:
        base_url = str(request.base_url).rstrip("/")
        generated_dir = os.path.join("outputs", "generated")
        if os.path.exists(generated_dir):
            files = [f for f in os.listdir(generated_dir) if f.endswith(('.png', '.jpg', '.jpeg'))]
            images = [f"{base_url}/outputs/generated/{f}" for f in files]
        else:
            images = []
        return JSONResponse(status_code=200, content={
            "status": "success",
            "images": images,
            "count": len(images)
        })
    except Exception as e:
        return JSONResponse(status_code=500, content={
            "status": "error",
            "message": f"Server error: {str(e)}"
        })

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)