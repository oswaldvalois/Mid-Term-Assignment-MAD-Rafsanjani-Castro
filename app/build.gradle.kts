plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.oswaldvalois.madmidterm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.oswaldvalois.madmidterm"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
}