package com.example.shuffle_cafe

sealed class Screen(val route: String) {
    object MainScreen: Screen("main_screen")
    object MapScreen: Screen("map_screen")
}