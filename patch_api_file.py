import re

with open("app/src/main/java/com/example/api/VtonApiService.kt", "r") as f:
    content = f.read()

replacement = """    private fun getFileFromUriOrUrl(context: Context, uriString: String, prefix: String): File? {
        if (uriString.startsWith("/")) {
            val f = File(uriString)
            if (f.exists()) return f
        }
        return try {
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")"""

content = content.replace("""    private fun getFileFromUriOrUrl(context: Context, uriString: String, prefix: String): File? {
        return try {
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")""", replacement)

with open("app/src/main/java/com/example/api/VtonApiService.kt", "w") as f:
    f.write(content)
print("Patched VtonApiService file access")
