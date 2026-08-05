import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

replacement = """                onSaveFavorite = { isFav ->
                    scope.launch(Dispatchers.IO) {
                        dao.updateFavorite(generatedLook.id, isFav)
                        if (isFav) {
                            com.example.api.StorageManager.copyFileToFavorites(context, generatedLook.resultImageUri)
                        }
                    }
                },"""

content = content.replace("""                onSaveFavorite = { isFav ->
                    scope.launch(Dispatchers.IO) {
                        dao.updateFavorite(generatedLook.id, isFav)
                    }
                },""", replacement)

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)
print("Patched AppNavigation Favorites")
