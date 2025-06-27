plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "htw.university.sharedbill"
    compileSdk = 35

    defaultConfig {
        applicationId = "htw.university.sharedbill"
        minSdk = 30
        targetSdk = 35
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
    implementation(libs.appcompat) { exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout") }
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment) { exclude(group = "androidx.coordinatorlayout", module = "coordinatorlayout") }
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.mockito:mockito-core:4.+")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.5.1")


    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


