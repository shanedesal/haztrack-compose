import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Backend base URL is machine-specific (points at your locally-run upload service) and must
// never be hardcoded in source. Falls back to the Android emulator's alias for the host
// machine's localhost if not set, so the project still builds without any local.properties entry.
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val backendBaseUrl: String =
    localProperties.getProperty("BACKEND_BASE_URL") ?: "http://10.0.2.2:4000/api/v1/"

android {
    namespace = "com.danger.haztrack"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.danger.haztrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    // Included in all build types so NetworkModule compiles for release too; only *activated*
    // (added to the OkHttpClient) when BuildConfig.DEBUG is true, so bodies are never logged
    // in a release build.
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.libphonenumber.android)
    ksp(libs.hilt.compiler)
    ksp(libs.moshi.kotlin.codegen)
}

// Moshi codegen already runs via KSP. Hilt's hiltJavaCompile* tasks still mirror every KSP
// processor onto javac's annotationProcessorPath (dagger#4116), which loads Moshi's
// deprecated kapt processor and prints a false "migrate to KSP" warning. Strip that jar
// from those tasks only; KSP codegen is unaffected.
tasks.withType<JavaCompile>().configureEach {
    if (name.startsWith("hiltJavaCompile")) {
        doFirst {
            options.annotationProcessorPath = options.annotationProcessorPath?.filter { file ->
                !file.name.startsWith("moshi-kotlin-codegen-")
            }
        }
    }
}