package com.example.data

import kotlinx.serialization.Serializable

@Serializable
object Splash
@Serializable
object Welcome
@Serializable
object PhotoSetup
@Serializable
object Home
@Serializable
object Wardrobe
@Serializable
object TryOnSetup
@Serializable
data class TryOn(val topImageUrl: String?, val bottomImageUrl: String?)
@Serializable
data class Result(val generatedImageUrl: String)
@Serializable
object Letter
@Serializable
object Favorites
@Serializable
object SavedLooks

