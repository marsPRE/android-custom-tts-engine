package com.example.dummytts.ui // Passe diesen Paketnamen an!

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dummytts.data.PrefKeys // Passe diesen Import an!
import com.example.dummytts.data.settingsDataStore // Passe diesen Import an!
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit

@OptIn(ExperimentalMaterial3Api::class) // Für TopAppBar, ExposedDropdownMenuBox etc.
@Composable
fun SettingsScreen(
    // Callback-Funktion, um zurück zu navigieren
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    // Lokaler Zustand für die Textfelder
    var urlState by remember { mutableStateOf("") }
    var apiKeyState by remember { mutableStateOf("") }
    var modelState by remember { mutableStateOf("") }
    var voiceState by remember { mutableStateOf("") }
    // Zustand, um anzuzeigen, ob Daten geladen werden
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope() // Scope für DataStore-Operationen
    val snackbarHostState = remember { SnackbarHostState() } // Für Feedback

    // Standardwerte definieren
    val defaultUrl = "https://api.openai.com/v1/audio/speech"
    val defaultModel = "tts-1" // Standard OpenAI Modell
    val defaultVoice = "alloy" // Standard OpenAI Stimme

    // Effekt zum Laden der aktuellen Werte beim Start des Screens
    LaunchedEffect(Unit) {
        isLoading = true
        // Versuche, die gespeicherten Einstellungen zu laden
        context.settingsDataStore.data.firstOrNull()?.let { prefs ->
            urlState = prefs[PrefKeys.BACKEND_URL] ?: defaultUrl
            apiKeyState = prefs[PrefKeys.API_KEY] ?: ""
            modelState = prefs[PrefKeys.TTS_MODEL] ?: defaultModel
            voiceState = prefs[PrefKeys.TTS_VOICE] ?: defaultVoice
            Log.d("SettingsScreen", "Initial values loaded from DataStore.")
        } ?: run {
            // Fallback, falls DataStore leer ist (z.B. erster Start)
            urlState = defaultUrl
            modelState = defaultModel
            voiceState = defaultVoice
            Log.d("SettingsScreen", "Using default values (DataStore might be empty).")
        }
        isLoading = false
    }

    // Listen der verfügbaren Optionen (für optionale Dropdowns)
    val models = listOf("tts-1", "tts-1-hd") // Beispiel OpenAI Modelle
    val voices = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer") // OpenAI Stimmen

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backend Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Zurück-Button
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues -> // paddingValues vom Scaffold verwenden

        if (isLoading) {
            // Einfache Ladeanzeige zentriert
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Eigentliche Einstellungs-UI in einer scrollbaren Spalte
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding vom Scaffold anwenden
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Zusätzliches Padding
                    .verticalScroll(rememberScrollState()) // Ermöglicht Scrollen
            ) {
                Text(
                    "Gib hier die Details für dein OpenAI-kompatibles TTS-Backend ein.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- URL Eingabe ---
                OutlinedTextField(
                    value = urlState,
                    onValueChange = { urlState = it },
                    label = { Text("Backend URL") },
                    placeholder = { Text(defaultUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- API Key Eingabe ---
                OutlinedTextField(
                    value = apiKeyState,
                    onValueChange = { apiKeyState = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Modell Eingabe ---
                // TODO: Für bessere UX durch ExposedDropdownMenuBox ersetzen (siehe unten)
                OutlinedTextField(
                    value = modelState,
                    onValueChange = { modelState = it },
                    label = { Text("TTS Model") },
                    placeholder = { Text(defaultModel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Alternative als Dropdown:
                /*
                var modelExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = modelExpanded,
                    onExpandedChange = { modelExpanded = !modelExpanded },
                     modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = modelState,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("TTS Model") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        models.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    modelState = selectionOption
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }
                */

                Spacer(modifier = Modifier.height(16.dp))

                // --- Stimme Eingabe ---
                // TODO: Für bessere UX durch ExposedDropdownMenuBox ersetzen
                OutlinedTextField(
                    value = voiceState,
                    onValueChange = { voiceState = it },
                    label = { Text("TTS Voice") },
                    placeholder = { Text(defaultVoice) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Hier könnte das Dropdown für Stimmen stehen, analog zum Modell-Dropdown

                Spacer(modifier = Modifier.height(24.dp))

                // --- Speicher-Button ---
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // Trimmen der URL, Modell, Stimme. API Key NICHT trimmen!
                                val urlToSave = urlState.trim()
                                val modelToSave = modelState.trim()
                                val voiceToSave = voiceState.trim()

                                if (urlToSave.isBlank() || modelToSave.isBlank() || voiceToSave.isBlank()) {
                                    snackbarHostState.showSnackbar("Bitte alle Felder (außer API Key) ausfüllen.")
                                    return@launch
                                }

                                context.settingsDataStore.edit { settings ->
                                    settings[PrefKeys.BACKEND_URL] = urlToSave
                                    settings[PrefKeys.API_KEY] = apiKeyState // Key nicht trimmen
                                    settings[PrefKeys.TTS_MODEL] = modelToSave
                                    settings[PrefKeys.TTS_VOICE] = voiceToSave
                                }
                                Log.i("SettingsScreen", "Settings saved!")
                                snackbarHostState.showSnackbar("Einstellungen gespeichert!")
                            } catch (e: Exception) {
                                Log.e("SettingsScreen", "Failed to save settings", e)
                                snackbarHostState.showSnackbar("Fehler beim Speichern!")
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End) // Button rechts ausrichten
                ) {
                    Text("Speichern")
                }

            } // Ende Column
        } // Ende else (isLoading)
    } // Ende Scaffold
} // Ende SettingsScreen

// Optional: Preview für den Settings Screen
//@Preview(showBackground = true, widthDp = 360, heightDp = 640)
//@Composable
//fun SettingsScreenPreview() {
//    // Hier könntest du ein Theme wrappen, falls nötig
//    SettingsScreen(onNavigateBack = {})
//}