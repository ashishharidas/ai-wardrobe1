import httpx
from typing import Optional
from config import POLLINATIONS_API_KEY, POLLINATIONS_API_URL, PREFERRED_MODELS
from prompts import get_vton_prompt

class RateLimitExceeded(Exception):
    pass

async def generate_try_on_image(person_b64: str, top_b64: Optional[str], bottom_b64: Optional[str]) -> tuple[Optional[str], Optional[str], Optional[str]]:
    """
    Returns (image_data_uri_or_url, model_used, error_msg) or raises an error.
    """
    headers = {
        "Content-Type": "application/json"
    }
    if POLLINATIONS_API_KEY:
        headers["Authorization"] = f"Bearer {POLLINATIONS_API_KEY}"
        
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

    payload = {
        "model": "openai",
        "messages": [
            {
                "role": "user",
                "content": prompt_content
            }
        ]
    }

    errors = []
    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            response = await client.post(POLLINATIONS_API_URL, headers=headers, json=payload)
            
            if response.status_code == 200:
                res_data = response.json()
                message = res_data.get("choices", [{}])[0].get("message", {})
                
                content = message.get("content", "")
                
                # Extract image URL from markdown or raw URL
                import re
                urls = re.findall(r'(https?://[^\s)"]+)', content)
                if urls:
                    return urls[0], "openai", None
                    
                errors.append(f"openai OK but no image. Content: {content[:150]}")
                
            elif response.status_code in [402, 429] or "rate limit" in response.text.lower():
                raise RateLimitExceeded("Pollinations API rate limit reached.")
            else:
                errors.append(f"openai error {response.status_code}: {response.text[:200]}")
                
        except Exception as e:
            errors.append(f"openai exception: {str(e)}")
            print(f"Pollinations failed: {e}")
            
    return None, None, " | ".join(errors)
