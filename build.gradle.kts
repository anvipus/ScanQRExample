// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.kapt) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.gms.plugin) apply false
    alias(libs.plugins.navigation.plugin) apply false
    alias(libs.plugins.perf.plugin) apply false
    alias(libs.plugins.distribution.plugin) apply false
    alias(libs.plugins.devtools.ksp) apply false
}