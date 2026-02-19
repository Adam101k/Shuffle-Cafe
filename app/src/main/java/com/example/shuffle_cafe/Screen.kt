package com.example.shuffle_cafe

sealed class Screen(val route: String) {
    object MainScreen: Screen("main_screen")
    object MapScreen: Screen("map_screen")
    object BookmarkScreen: Screen("bookmark_screen")
    object ProfileScreen : Screen("profile_screen")

    object CafeDetails : Screen("cafe_details/{cafeId}") {
        fun createRoute(cafeId: String) = "cafe_details/$cafeId"
    }

    object WriteReview : Screen("write_review/{cafeId}") {
        fun createRoute(cafeId: String) = "write_review/$cafeId"
    }
    object LoginScreen: Screen("login_screen")
}
