plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.bugsnag.android)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.xda.nachonotch"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.xda.nachonotch"
        minSdk = 24
        targetSdk = 34
        versionCode = 34
        versionName = versionCode.toString()

        resValue("string", "applicationId", "$applicationId")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes.addAll(arrayOf("META-INF/atomicfu.kotlin_module", "META-INF/library_release.kotlin_module"))
    }

    buildFeatures {
        viewBinding = true
        aidl = true
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //AndroidX
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.activity.compose)

    //Google
    implementation(libs.material)

    //Mine
    implementation(libs.seekBarPreference)

    //Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.compiler)

    //Other
    implementation(libs.markwon)
    implementation(libs.taskerpluginlibrary)
    implementation(libs.hiddenapibypass)
    implementation(libs.bugsnag.android)
    implementation(libs.bugsnag.android.performance)
}
