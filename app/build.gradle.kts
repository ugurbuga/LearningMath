plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ugurbuga.learningmath"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ugurbuga.learningmath"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/LearningMath.jks")
            storePassword = "LearningMath"
            keyAlias = "LearningMath"
            keyPassword = "LearningMath"
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.components.resources)
    debugImplementation(libs.compose.ui.tooling)
}

tasks.register("run") {
    group = "application"
    description = "Runs the desktop application"
    dependsOn(":shared:run")
}
