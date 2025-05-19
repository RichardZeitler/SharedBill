plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "htw.university.sharedbill"
    compileSdk = 35

    defaultConfig {
        applicationId = "htw.university.sharedbill"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat) {
        exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout")
    }
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment) {
        exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout")
    }
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
