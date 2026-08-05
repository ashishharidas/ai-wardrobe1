import re

with open("app/src/main/java/com/example/data/WardrobeDao.kt", "r") as f:
    content = f.read()

replacement = """    @Query("SELECT * FROM generated_looks WHERE resultImageUri = :uri LIMIT 1")
    suspend fun getLookByUri(uri: String): GeneratedLook?

    @Query("UPDATE generated_looks SET isFavorite = :isFavorite WHERE id = :id")"""

content = content.replace("""    @Query("UPDATE generated_looks SET isFavorite = :isFavorite WHERE id = :id")""", replacement)

with open("app/src/main/java/com/example/data/WardrobeDao.kt", "w") as f:
    f.write(content)
print("Patched WardrobeDao URI")
