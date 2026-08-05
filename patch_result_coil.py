import re

with open("app/src/main/java/com/example/ui/screens/ResultScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Uri.parse(look.resultImageUri)", "look.resultImageUri")
content = content.replace("Uri.parse(bodyPhotoUri)", "bodyPhotoUri")

with open("app/src/main/java/com/example/ui/screens/ResultScreen.kt", "w") as f:
    f.write(content)
print("Patched ResultScreen Coil")
