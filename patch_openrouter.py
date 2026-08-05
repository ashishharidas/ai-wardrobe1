import re
with open("backend/openrouter_service.py", "r") as f:
    content = f.read()

# Update return type hint
content = content.replace("tuple[Optional[str], Optional[str]]", "tuple[Optional[str], Optional[str], Optional[str]]")

# Update return values
content = content.replace("return images[0], model_id", "return images[0], model_id, None")
content = content.replace("return urls[0], model_id", "return urls[0], model_id, None")
content = content.replace("return None, None", "return None, None, \" | \".join(errors)")

# Add error tracking
# First add errors list before the client block
content = content.replace("limit_exceeded = False", "limit_exceeded = False\n    errors = []")

# Capture failed HTTP responses
new_try_block = """            try:
                response = await client.post(OPENROUTER_API_URL, headers=headers, json=payload)
                if response.status_code == 200:
                    res_data = response.json()
                    message = res_data.get("choices", [{}])[0].get("message", {})
                    
                    images = message.get("images", [])
                    if images:
                        return images[0], model_id, None
                                        
                    content = message.get("content", "")
                    if "![image](" in content or "http" in content:
                        import re
                        urls = re.findall(r'(https?://[^\s)"]+)', content)
                        if urls:
                            return urls[0], model_id, None
                    errors.append(f"{model_id} OK but no image. Content: {content[:150]}")
                elif response.status_code in [402, 429] or "rate limit" in response.text.lower() or "quota" in response.text.lower():
                    limit_exceeded = True
                    errors.append(f"{model_id} rate limited.")
                else:
                    errors.append(f"{model_id} error {response.status_code}: {response.text}")
                    
            except Exception as e:
                errors.append(f"{model_id} exception: {str(e)}")"""

# Regex substitute the old try-except block
pattern = re.compile(r"            try:\n.*?print\(f\"Model \{model_id\} failed: \{e\}\"\)", re.DOTALL)
content = pattern.sub(new_try_block, content)

with open("backend/openrouter_service.py", "w") as f:
    f.write(content)
print("Patched openrouter_service.py")
