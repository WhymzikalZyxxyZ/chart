plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
}

// Release signing is optional at build time: local/PR builds work unsigned,
// the release workflow supplies these via env vars sourced from repo secrets.
val releaseStoreFile     = System.getenv("RELEASE_STORE_FILE")
val releaseStorePassword = System.getenv("RELEASE_STORE_PASSWORD")
val releaseKeyAlias      = System.getenv("RELEASE_KEY_ALIAS")
val releaseKeyPassword   = System.getenv("RELEASE_KEY_PASSWORD")
val hasReleaseSigning    = !releaseStoreFile.isNullOrBlank()

android {
    namespace   = "xyz.zyxwonderland.chart"
    compileSdk  = 35

    defaultConfig {
        applicationId = "xyz.zyxwonderland.chart"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 2
        versionName   = "0.1.1"

        // SMART App Launch redirect URI scheme (AppAuth Custom Tabs callback).
        // No auth code is wired up yet — see docs/adr/002-fhir-source-and-auth.md —
        // this just reserves the manifest placeholder AppAuth's Gradle plugin needs.
        manifestPlaceholders["appAuthRedirectScheme"] = "xyz.zyxwonderland.chart"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile     = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias      = releaseKeyAlias
                keyPassword   = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.core.ktx)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.appauth)
    implementation(libs.security.crypto)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
}
