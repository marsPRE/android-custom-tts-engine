package com.example.CustomTts.data // Stelle sicher, dass der Paketname korrekt ist

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey // <-- Import hinzufügen!

// DataStore Instanz (von Schritt 1)
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "tts_settings")

// Objekt zum Halten der Schlüssel hinzufügen
object PrefKeys {
    // Ein Schlüssel für die Backend-URL (Typ: String)
    val BACKEND_URL = stringPreferencesKey("backend_url")

    // Ein Schlüssel für den API-Key (Typ: String)
    val API_KEY = stringPreferencesKey("api_key")

    // Ein Schlüssel für das Model (Typ: String)
    val TTS_MODEL = stringPreferencesKey("tts_model")

    // Ein Schlüssel für die Stimme (Typ: String)
    val TTS_VOICE = stringPreferencesKey("tts_voice")

    // Hier könntest du bei Bedarf weitere Schlüssel für andere Einstellungen hinzufügen
}