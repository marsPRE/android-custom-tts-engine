plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "1.9.23"
    alias(libs.plugins.kotlin.android) // Oder aktuellste Version prüfen
}

android {
    namespace = "com.example.dummytts"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dummytts"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Ktor Client (CIO Engine ist eine gute Wahl für Android)
    val ktorVersion = "2.3.12" // Oder aktuellste Version prüfen
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion") // Für JSON
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion") // JSON Serializer
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1") // Oder aktuellste Version prüfen

    // Optional aber empfohlen: Lifecycle Scope für Coroutines im Service
    implementation("androidx.lifecycle:lifecycle-service:2.8.4") // Oder aktuellste Version prüfen

}