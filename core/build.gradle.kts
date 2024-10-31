plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    namespace = "com.anvipus.core"
    compileSdk = 34

    /*buildFeatures {
        buildConfig = true
    }*/
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            val KUNCI_GARAM: String by project
            isMinifyEnabled = false
            buildConfigField("String", "KUNCI_GARAM", KUNCI_GARAM)
            resValue("string", "app_name_config", "DEV Android Explore")
        }
        release {
            val KUNCI_GARAM: String by project
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "KUNCI_GARAM", KUNCI_GARAM)
            resValue("string", "app_name_config", "Android Explore")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dataBinding {
        enable = true
    }
}

dependencies {
    //android sdk
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.common.java8)
    kapt(libs.androidx.lifecycle.common.java8)
    api(libs.androidx.appcompat)
    api(libs.androidx.worker)
    api(libs.androidx.paging)
    api(libs.androidx.security.crypto)
    api(libs.material)
    api(libs.navigation.ui.ktx)
    api(libs.navigation.runtime.ktx)
    api(libs.navigation.fragment.ktx)
    api(libs.recycler.view)
    api(libs.viewpager)

    // firebase
    api(platform(libs.firebase.bom))
    api(libs.firebase.crashlytics.ktx)
    api(libs.firebase.analytics.ktx)
    api(libs.firebase.messaging.ktx)
    api(libs.firebase.perf.ktx)
    api(libs.firebase.appcheck.playintegrity)
    api(libs.firebase.appcheck.debug)
    api(libs.firebase.appcheck.ktx)

    //compose
    api(libs.androidx.activity.compose)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.material3)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.constraint)
    api(libs.navigation.compose)
    api(libs.accompanist.flowlayout)
    api(libs.accompanist.permissions)

    //retrofit
    api(libs.retrofit)
    api(libs.retrofit.converter.moshi)

    //moshi
    api(libs.moshi)
    api(libs.moshi.codegen)

    // okhttp
    api(platform(libs.okhttp))
    api(libs.okhttp.module)
    api(libs.okhttp.logging.interceptor)

    //glide
    api(libs.glide)
    api(libs.glide.okhttp3)
    kapt(libs.glide.compiler.kapt)

    //gson
    api(libs.gson)

    //dagger
    api(libs.dagger)
    api(libs.dagger.android)
    api(libs.dagger.android.support)

    //room
    api(libs.room)

    //jwt
    api(libs.jwt.decode)

    //biometric
    api(libs.biometric)

    //lottie
    api(libs.lottie)
    api(libs.lottie.compose)

    //shimmer
    api(libs.shimmer)

    //coil
    api(libs.coil.gif)
    api(libs.coil.compose)

    //camerax
    api(libs.camera.core)
    api(libs.camera.camera2)
    api(libs.camera.lifecycle)
    api(libs.camera.view)

    //ml kit barcode scanning
    api(libs.ml.kit.barcode.scanning)

    //timber
    api(libs.timber)

    //zxing
    api(libs.zxing)
    api(libs.journey.zxing)

    //play service
    api(libs.play.services.auth)
    api(libs.play.services.location)
    api(libs.play.services.auth.api.phone)

    //chucker
    debugApi(libs.chucker.debug)
    releaseApi(libs.chucker.release)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}