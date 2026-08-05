import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator"""

content = content.replace("import androidx.compose.runtime.Composable", imports + "\nimport androidx.compose.runtime.Composable")

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)
print("Added imports")
