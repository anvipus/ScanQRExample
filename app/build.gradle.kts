plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.navigation.plugin)
}

android {
    namespace = "com.anvipus.explore"
    compileSdk = Integer.parseInt(libs.versions.compile.get())

    defaultConfig {
        applicationId = "com.anvipus.explore"
        minSdk = Integer.parseInt(libs.versions.minimum.get())
        targetSdk = Integer.parseInt(libs.versions.target.get())
        versionCode = Integer.parseInt(libs.versions.version.code.get())
        versionName = libs.versions.version.name.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    dataBinding {
        enable = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES.txt"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/license.txt"
        }
    }
}

dependencies {
    api(project(":core"))

    kapt(libs.dagger.compiler.kapt)
    kapt(libs.dagger.android.processor.kapt)
    kapt(libs.moshi.kotlin.codegen.kapt)
    kapt(libs.room.compiler.kapt)
    kapt(libs.lifecycle.common.java8.kapt)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}