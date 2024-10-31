package com.anvipus.explore.ui.compose.scanqr

sealed class Screen(val route: String) {
    object CameraScreen : Screen("camera_screen")
}