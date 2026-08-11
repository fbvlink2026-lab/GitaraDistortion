plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.gitaradistortion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gitaradistortion"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // ✅ Suporta sa lahat ng cellphone
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }

        // ✅ IKABIT ANG AUDIO C++
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        release { isMinifyEnabled = false }
        debug { isDebuggable = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }

    // ✅ BUILD NA MAY C++
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
