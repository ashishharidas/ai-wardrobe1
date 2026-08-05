import re

with open("app/src/main/java/com/example/ui/screens/WardrobeScreen.kt", "r") as f:
    content = f.read()

replacement = """    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val dir = if (selectedCategoryIndex == 0) com.example.api.StorageManager.getTopsDir(context) else com.example.api.StorageManager.getBottomsDir(context)
                    val savedFile = com.example.api.StorageManager.saveInputStreamToDir(inputStream, dir, if (selectedCategoryIndex == 0) "top" else "bottom")
                    val newOutfit = Outfit(
                        imageUrl = savedFile.absolutePath,
                        category = currentCategoryName,
                        name = "$currentCategoryName Item"
                    )
                    dao.insertOutfit(newOutfit)
                }
            }
        }
    }"""

pattern = re.compile(r"    val imagePickerLauncher.*?dao\.insertOutfit\(newOutfit\)\n            \}\n        \}\n    \}", re.DOTALL)
new_content = pattern.sub(replacement, content)

with open("app/src/main/java/com/example/ui/screens/WardrobeScreen.kt", "w") as f:
    f.write(new_content)
print("Patched WardrobeScreen")
