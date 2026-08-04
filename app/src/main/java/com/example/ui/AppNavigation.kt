package com.example.ui

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
                        userPreferences.completeSetup(uri)
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
                onResultReady = { resultUrl ->
                    scope.launch(Dispatchers.IO) {
                        dao.insertLook(
                            GeneratedLook(
                                topImageUrl = args.topImageUrl,
                                bottomImageUrl = args.bottomImageUrl,
                                resultImageUri = resultUrl
                            )
                        )
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
            val generatedLook = GeneratedLook(
                id = 1,
                resultImageUri = resultUrl
            )
            
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
                        dao.updateFavorite(generatedLook.id, isFav)
                    }
                },
                onGenerateAgain = { navController.popBackStack() }
            )
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
