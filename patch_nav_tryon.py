import re

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "r") as f:
    content = f.read()

replacement = """        composable<TryOn> { backStackEntry ->
            val args = backStackEntry.toRoute<TryOn>()
            TryOnScreen(
                bodyPhotoUri = bodyPhotoUri,
                topImageUrl = args.topImageUrl,
                bottomImageUrl = args.bottomImageUrl,
                apiService = apiService,
                dao = dao,
                onResultReady = { resultUrl ->
                    scope.launch(Dispatchers.IO) {
                        // Check if we need to insert (only if it wasn't already in DB)
                        val existing = dao.getLookByCombination(args.topImageUrl, args.bottomImageUrl)
                        if (existing == null) {
                            dao.insertLook(
                                GeneratedLook(
                                    topImageUrl = args.topImageUrl,
                                    bottomImageUrl = args.bottomImageUrl,
                                    resultImageUri = resultUrl
                                )
                            )
                        }
                    }
                    val encodedUrl = java.net.URLEncoder.encode(resultUrl, "UTF-8")
                    navController.navigate(Result(encodedUrl)) {
                        popUpTo<TryOn> { inclusive = true }
                    }
                },
                onError = {
                    navController.popBackStack()
                }
            )
        }"""

pattern = re.compile(r"        composable<TryOn> \{ backStackEntry ->\n.*?onError = \{\n                    navController\.popBackStack\(\)\n                \}\n            \)\n        \}", re.DOTALL)
content = pattern.sub(replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AppNavigation.kt", "w") as f:
    f.write(content)
print("Patched AppNavigation TryOn")
