import re

with open("app/src/main/java/com/example/data/WardrobeDao.kt", "r") as f:
    content = f.read()

replacement = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLook(look: GeneratedLook)

    @Query("SELECT * FROM generated_looks WHERE (topImageUrl = :topUrl OR (topImageUrl IS NULL AND :topUrl IS NULL)) AND (bottomImageUrl = :bottomUrl OR (bottomImageUrl IS NULL AND :bottomUrl IS NULL)) LIMIT 1")
    suspend fun getLookByCombination(topUrl: String?, bottomUrl: String?): GeneratedLook?

    @Query("UPDATE generated_looks SET isFavorite = :isFavorite WHERE id = :id")"""

content = content.replace("""    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLook(look: GeneratedLook)

    @Query("UPDATE generated_looks SET isFavorite = :isFavorite WHERE id = :id")""", replacement)

with open("app/src/main/java/com/example/data/WardrobeDao.kt", "w") as f:
    f.write(content)
print("Patched WardrobeDao")
