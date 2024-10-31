package com.anvipus.explore.ui.compose.scanqr

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.anvipus.core.utils.theme.MyExploreTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.anvipus.core.R
import com.anvipus.explore.base.BaseFragmentCompose
import com.codedisruptors.dabestofme.di.Injectable

@ExperimentalPermissionsApi
class ScanQrComposeFragment : BaseFragmentCompose(), Injectable {
    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean
        get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_mlkit_compose_screen

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MyExploreTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = Screen.CameraScreen.route
                            ) {
                                composable(
                                    route = Screen.CameraScreen.route
                                ) {
                                    CameraPreview(navController())
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun MultiplePermissions(navController: NavHostController) {
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    permissionStates.launchMultiplePermissionRequest()
                }

                Lifecycle.Event.ON_CREATE -> {

                }
                Lifecycle.Event.ON_RESUME -> {

                }
                Lifecycle.Event.ON_PAUSE -> {

                }
                Lifecycle.Event.ON_STOP -> {

                }
                Lifecycle.Event.ON_DESTROY -> {

                }
                Lifecycle.Event.ON_ANY -> {

                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        permissionStates.permissions.forEach { it ->
            when (it.permission) {
                Manifest.permission.CAMERA -> {
                    when {
                        it.hasPermission -> {
                            /* Permission has been granted by the user.
                               You can use this permission to now acquire the location of the device.
                               You can perform some other tasks here.
                            */
//                            Text(text = "Read Ext Storage permission has been granted")
                        }
                        it.shouldShowRationale -> {
                            /*Happens if a user denies the permission two times

                             */
                            Text(text = "Read Ext Storage permission is needed")
                        }
                        !it.hasPermission && !it.shouldShowRationale -> {
                            /* If the permission is denied and the should not show rationale
                                You can only allow the permission manually through app settings
                             */
                            Text(text = "Navigate to settings and enable the Storage permission")

                        }
                    }
                }
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    when {
                        it.hasPermission -> {
                            /* Permission has been granted by the user.
                               You can use this permission to now acquire the location of the device.
                               You can perform some other tasks here.
                            */
                            MainScreen(navController = navController)
                        }
                        it.shouldShowRationale -> {
                            /*Happens if a user denies the permission two times

                             */
                            Text(text = "Location permission is needed")

                        }
                        !it.hasPermission && !it.shouldShowRationale -> {
                            /* If the permission is denied and the should not show rationale
                                You can only allow the permission manually through app settings
                             */
                            Text(text = "Navigate to settings and enable the Location permission")

                        }
                    }
                }
            }
        }
    }
}
