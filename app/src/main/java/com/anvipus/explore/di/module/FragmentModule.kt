package com.anvipus.explore.di.module

import com.anvipus.explore.ui.compose.scanqr.ScanQrComposeFragment
import com.anvipus.explore.ui.xml.MainFragment
import com.anvipus.explore.ui.xml.scanqr.ScanQrCameraFragment
import com.anvipus.explore.ui.xml.scanqr.ScanQrCameraZxingFragment
import com.anvipus.explore.ui.xml.scanqr.ScanQrFragment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun main(): MainFragment

    @ContributesAndroidInjector
    abstract fun scanQR(): ScanQrFragment

    @ContributesAndroidInjector
    abstract fun scanQRCamera(): ScanQrCameraFragment

    @ContributesAndroidInjector
    abstract fun scanQRZxingCamera(): ScanQrCameraZxingFragment

    @OptIn(ExperimentalPermissionsApi::class)
    @ContributesAndroidInjector
    abstract fun scanQRCompose(): ScanQrComposeFragment

}