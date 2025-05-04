// Top-level build file where you can add configuration options common to all sub-projects/modules.

// HINWEIS: libs.plugins... verweist auf Einträge in deiner gradle/libs.versions.toml Datei.
// Stelle sicher, dass diese Aliase dort korrekt definiert sind.
plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "1.9.23" // Behalte die Version hier oder verwalte sie auch zentral
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    //id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    // Optional: Wenn du KSP für Room verwendest (war im Originalprojekt)
    // alias(libs.plugins.ksp)
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
        // Vector Drawables aktivieren (wird oft mit Compose/Material benötigt)
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Für Tests erstmal deaktiviert lassen
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Korrigierte Compile Options auf Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Korrigierte Kotlin Options auf JVM Target 17
    kotlinOptions {
        jvmTarget = "17"
    }

    // Build Features für Compose aktivieren
    buildFeatures {
        compose = true
    }

    // Compose Options mit korrekter Compiler Version hinzufügen
    composeOptions {
        // Korrekte Compiler-Version für Kotlin 2.0.x (Annahme: Kotlin 2.0.21 wird verwendet)
        // Prüfe die Kompatibilitätstabelle für deine exakte Kotlin-Version!
        kotlinCompilerExtensionVersion = "2.0.20" // Z.B. für Kotlin 2.0.0 / 2.0.21
    }

    // Packaging Options hinzufügen (verhindert manchmal Build-Fehler)
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin" // Ktor schließt das manchmal ein
        }
    }
} // Ende android { ... }

dependencies {

    // ---- Alte View-basierte Abhängigkeiten (Prüfen, ob noch benötigt!) ----
    // Wenn deine UI jetzt rein aus Compose besteht, sind diese ggf. nicht mehr nötig:
    // implementation(libs.appcompat) // Wahrscheinlich nicht mehr nötig mit ComponentActivity
    implementation(libs.material) // Die alte Material Design Bibliothek (nicht M3)
    // implementation(libs.constraintlayout) // Für ConstraintLayout in XML

    // ---- Kern-Abhängigkeiten (Bleiben) ----
    implementation(libs.core.ktx) // Kotlin Extensions
    implementation(libs.activity) // Basis für Activity (wird von activity-compose genutzt)

    // ---- Test-Abhängigkeiten (Bleiben) ----
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ---- Deine hinzugefügten Abhängigkeiten (Bleiben) ----
    // Ktor Client
    val ktorVersion = "2.3.12" // Oder aktuellste Version prüfen
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1") // Oder aktuellste Version prüfen

    // Lifecycle Scope für Service
    implementation("androidx.lifecycle:lifecycle-service:2.8.4") // Oder aktuellste Version prüfen

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Oder aktuellste Version

    // ---- Jetpack Compose Abhängigkeiten (Bleiben) ----
    val composeBomVersion = "2024.06.00" // Oder aktuellste Version prüfen
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))

    // Compose Module (ohne explizite Version dank BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Material 3
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.1") // Activity Compose braucht ggf. explizite Version

    // Debug Implementierungen für Compose
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest") // Optional für Tests

    // AndroidTest Implementierungen für Compose
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}