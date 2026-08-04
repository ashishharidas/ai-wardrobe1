import re

with open("backend/main.py", "r") as f:
    content = f.read()

pattern = re.compile(r"def save_generated_image\(image_data: str\) -> str:.*?return image_data", re.DOTALL)

replacement = """def save_generated_image(image_data: str, request: Request) -> str:
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
                
    return image_data"""

new_content = pattern.sub(replacement, content)
with open("backend/main.py", "w") as f:
    f.write(new_content)
print("Regex replaced!")
