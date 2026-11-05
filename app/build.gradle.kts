plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.nav.safe.args)
}

android {
    namespace = "io.idnow.docidv.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.idnow.docidv.sample"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.19.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.constraintlayout)
    implementation(libs.viewbinding)
    implementation(libs.activityKtx)

    // Import the DocIDV BOM
    implementation(platform(libs.idnow.docidv.bom))

    // Import DocIDV Core + AI
    implementation(libs.idnow.docidv.core)
    implementation(libs.idnow.docidv.ai)

    // Optional - German eID
    implementation(libs.idnow.docidv.eid.governikus)
}