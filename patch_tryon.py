import re

with open("app/src/main/java/com/example/ui/screens/TryOnScreen.kt", "r") as f:
    content = f.read()

# Add dao argument
content = content.replace(
    "apiService: VtonApiService,",
    "apiService: VtonApiService,\n    dao: com.example.data.WardrobeDao,"
)

# Add DB check logic
launched_effect = """    LaunchedEffect(Unit) {
        if (bodyPhotoUri != null) {
            try {
                // Check local database first
                val existingLook = dao.getLookByCombination(topImageUrl, bottomImageUrl)
                if (existingLook != null) {
                    onResultReady(existingLook.resultImageUri)
                    return@LaunchedEffect
                }

                val resultUrl = apiService.generateTryOn(context, bodyPhotoUri, topImageUrl, bottomImageUrl)
                if (resultUrl != null) {
                    onResultReady(resultUrl)
                } else {
                    android.widget.Toast.makeText(context, "Failed to generate image.", android.widget.Toast.LENGTH_LONG).show()
                    onError()
                }
            } catch (e: Exception) {
                val msg = e.message ?: "An error occurred during generation."
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                onError()
            }
        } else {
            onError()
        }
    }"""

pattern = re.compile(r"    LaunchedEffect\(Unit\) \{.*?    \}", re.DOTALL)
content = pattern.sub(launched_effect, content, count=1)

with open("app/src/main/java/com/example/ui/screens/TryOnScreen.kt", "w") as f:
    f.write(content)
print("Patched TryOnScreen")
