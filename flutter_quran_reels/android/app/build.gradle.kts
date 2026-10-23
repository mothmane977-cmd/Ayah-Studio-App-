plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.aistudio.quranreels.nqrxvk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.quranreels.nqrxvk"
        // REQUIRED for ffmpeg_kit_flutter_min_gpl (minimum SDK 24 for GPL/x264 native binaries)
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    packaging {
        jniLibs {
            // Ensures FFmpeg native .so binaries from ffmpeg_kit_flutter_min_gpl extract cleanly
            useLegacyPackaging = true
            pickFirsts += listOf(
                "lib/**/libc++_shared.so",
                "lib/**/libffmpegkit.so",
                "lib/**/libffmpegkit_abidetect.so"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}
