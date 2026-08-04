import os

with open("backend/main.py", "r") as f:
    content = f.read()

# Remove save_upload calls
content = content.replace("save_upload(UPLOAD_DIR_PERSON, \"person\", person_bytes)", "# save_upload(UPLOAD_DIR_PERSON, \"person\", person_bytes)")
content = content.replace("save_upload(UPLOAD_DIR_TOPS, \"top\", top_bytes)", "# save_upload(UPLOAD_DIR_TOPS, \"top\", top_bytes)")
content = content.replace("save_upload(UPLOAD_DIR_BOTTOMS, \"bottom\", bottom_bytes)", "# save_upload(UPLOAD_DIR_BOTTOMS, \"bottom\", bottom_bytes)")

with open("backend/main.py", "w") as f:
    f.write(content)
print("Patched!")
