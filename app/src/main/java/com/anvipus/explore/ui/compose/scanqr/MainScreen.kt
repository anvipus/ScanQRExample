package com.anvipus.explore.ui.compose.scanqr

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun MainScreen(
    navController: NavHostController,
) {
    Column() {
        Button(
            onClick = {
                navController.navigate(Screen.CameraScreen.route)
            },

            ) {
            Text(text = "Open Camera")
        }
    }
}