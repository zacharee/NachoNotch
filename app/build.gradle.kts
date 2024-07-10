plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.bugsnag.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xda.nachonotch"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.xda.nachonotch"
        minSdk = 24
        targetSdk = 34
        versionCode = 40
        versionName = versionCode.toString()

        resValue("string", "applicationId", "$applicationId")

        base.archivesName.set("NachoNotch_${versionCode}")
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

    //Other
    implementation(libs.markwon)
    implementation(libs.taskerpluginlibrary)
    implementation(libs.hiddenapibypass)
    implementation(libs.bugsnag.android)
    implementation(libs.bugsnag.android.performance)
}
