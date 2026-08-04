import httpx
from typing import Optional
from config import OPENROUTER_API_KEY, OPENROUTER_API_URL, PREFERRED_MODELS
from prompts import get_vton_prompt

class RateLimitExceeded(Exception):
    pass

async def generate_try_on_image(person_b64: str, top_b64: Optional[str], bottom_b64: Optional[str]) -> tuple[Optional[str], Optional[str]]:
    """
    Returns (image_data_uri_or_url, model_used) or raises an error.
    """
    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "HTTP-Referer": "https://elan.fashion",
        "X-Title": "ÉLAN Wardrobe",
        "Content-Type": "application/json"
    }

    prompt_content = [
        {
            "type": "text",
            "text": get_vton_prompt()
        },
        {
            "type": "image_url",
            "image_url": {
                "url": f"data:image/jpeg;base64,{person_b64}"
            }
        }
    ]

    if top_b64:
        prompt_content.append({
            "type": "image_url",
            "image_url": {
                "url": f"data:image/jpeg;base64,{top_b64}"
            }
        })
        
    if bottom_b64:
        prompt_content.append({
            "type": "image_url",
            "image_url": {
                "url": f"data:image/jpeg;base64,{bottom_b64}"
            }
        })

    limit_exceeded = False

    async with httpx.AsyncClient(timeout=60.0) as client:
        for model_id in PREFERRED_MODELS:
            payload = {
                "model": model_id,
                "messages": [
                    {
                        "role": "user",
                        "content": prompt_content
                    }
                ],
                "modalities": ["image", "text"]
            }
            try:
                response = await client.post(OPENROUTER_API_URL, headers=headers, json=payload)
                if response.status_code == 200:
                    res_data = response.json()
                    message = res_data.get("choices", [{}])[0].get("message", {})
                    # Look for images in the response
                    # Wait, openrouter docs say generated images might be in an array `message.images`
                    images = message.get("images", [])
                    if images:
                        # Some endpoints return url or base64
                        return images[0], model_id
                    
                    # Also try parsing content for image url if they put it in text
                    content = message.get("content", "")
                    if "![image](" in content or "http" in content:
                        import re
                        urls = re.findall(r'(https?://[^\s)"]+)', content)
                        if urls:
                            return urls[0], model_id
                            
                elif response.status_code in [402, 429] or "rate limit" in response.text.lower() or "quota" in response.text.lower():
                    limit_exceeded = True
                    print(f"Limit Exceeded for {model_id}: {response.status_code}")
                    continue
                    
            except Exception as e:
                print(f"Model {model_id} failed: {e}")
                
    if limit_exceeded:
        raise RateLimitExceeded("OpenRouter API rate limit or model credit quota has been reached.")
        
    return None, None
