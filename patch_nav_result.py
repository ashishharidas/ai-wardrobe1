import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

replacement = """        composable<Result> { backStackEntry ->
            val args = backStackEntry.toRoute<Result>()
            val resultUrl = java.net.URLDecoder.decode(args.generatedImageUrl, "UTF-8")
            var generatedLook by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<GeneratedLook?>(null) }
            
            androidx.compose.runtime.LaunchedEffect(resultUrl) {
                generatedLook = dao.getLookByUri(resultUrl)
            }
            
            if (generatedLook != null) {
                ResultScreen(
                    bodyPhotoUri = bodyPhotoUri,
                    look = generatedLook,
                    onBack = { 
                        navController.navigate(Home) {
                            popUpTo(Home) { inclusive = true }
                        }
                    },
                    onSaveFavorite = { isFav ->
                        scope.launch(Dispatchers.IO) {
                            generatedLook?.let { look ->
                                dao.updateFavorite(look.id, isFav)
                                if (isFav) {
                                    com.example.api.StorageManager.copyFileToFavorites(context, look.resultImageUri)
                                }
                            }
                        }
                    },
                    onGenerateAgain = { navController.popBackStack() }
                )
            } else {
                // Loading or placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }"""

pattern = re.compile(r"        composable<Result> \{ backStackEntry ->\n.*?onGenerateAgain = \{ navController\.popBackStack\(\) \}\n            \)\n        \}", re.DOTALL)
content = pattern.sub(replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)
print("Patched AppNavigation Result")
