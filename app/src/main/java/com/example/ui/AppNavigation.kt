package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.api.VtonApiService
import com.example.data.AppDatabase
import com.example.data.Favorites
import com.example.data.GeneratedLook
import com.example.data.Home
import com.example.data.Letter
import com.example.data.PhotoSetup
import com.example.data.Result
import com.example.data.Splash
import com.example.data.TryOn
import com.example.data.TryOnSetup
import com.example.data.UserPreferences
import com.example.data.Wardrobe
import com.example.data.Welcome
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LetterScreen
import com.example.ui.screens.PhotoSetupScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TryOnScreen
import com.example.ui.screens.TryOnSetupScreen
import com.example.ui.screens.WardrobeScreen
import com.example.ui.screens.WelcomeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val hasCompletedSetup by userPreferences.hasCompletedSetup.collectAsState(initial = false)
    val bodyPhotoUri by userPreferences.bodyPhotoUri.collectAsState(initial = null)
    
    val apiService = remember { VtonApiService() }
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val dao = remember { database.wardrobeDao() }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                onNavigateNext = {
                    if (hasCompletedSetup) {
                        navController.navigate(Home) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Welcome) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable<Welcome> {
            WelcomeScreen(
                onNavigateNext = {
                    navController.navigate(PhotoSetup)
                }
            )
        }
        
        composable<PhotoSetup> {
            PhotoSetupScreen(
                onPhotoSaved = { uri ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
                            if (inputStream != null) {
                                val savedFile = com.example.api.StorageManager.saveInputStreamToDir(inputStream, context.filesDir, "body")
                                userPreferences.completeSetup(savedFile.absolutePath)
                            } else {
                                userPreferences.completeSetup(uri)
                            }
                        } catch (e: Exception) {
                            userPreferences.completeSetup(uri)
                        }
                    }
                    navController.navigate(Home) {
                        popUpTo(Welcome) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Home> {
            HomeScreen(
                bodyPhotoUri = bodyPhotoUri,
                onNavigateToTryOnSetup = {
                    navController.navigate(TryOnSetup)
                },
                onNavigateToWardrobe = {
                    navController.navigate(Wardrobe)
                },
                onNavigateToFavorites = {
                    navController.navigate(Favorites)
                },
                onNavigateToLetter = {
                    navController.navigate(Letter)
                }
            )
        }
        
        composable<Wardrobe> {
            WardrobeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<TryOnSetup> {
            TryOnSetupScreen(
                onBack = { navController.popBackStack() },
                onGenerate = { topUri, bottomUri ->
                    navController.navigate(TryOn(topUri, bottomUri))
                },
                onNavigateToWardrobe = {
                    navController.navigate(Wardrobe)
                }
            )
        }
        
        composable<TryOn> { backStackEntry ->
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
        }
        
        composable<Result> { backStackEntry ->
            val args = backStackEntry.toRoute<Result>()
            val resultUrl = java.net.URLDecoder.decode(args.generatedImageUrl, "UTF-8")
            val generatedLookState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<GeneratedLook?>(null) }
            
            androidx.compose.runtime.LaunchedEffect(resultUrl) {
                generatedLookState.value = dao.getLookByUri(resultUrl)
            }
            
            val generatedLook = generatedLookState.value
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
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
        
        composable<Favorites> {
            FavoritesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Letter> {
            LetterScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
