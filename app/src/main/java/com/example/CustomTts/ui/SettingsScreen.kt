package com.example.CustomTts.ui // Passe diesen Paketnamen an!

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
import androidx.compose.ui.res.stringResource // <-- Wichtiger Import!
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.CustomTts.R // Import für R.string...
import com.example.CustomTts.data.PrefKeys // Passe diesen Import an!
import com.example.CustomTts.data.settingsDataStore // Passe diesen Import an!
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var urlState by remember { mutableStateOf("") }
    var apiKeyState by remember { mutableStateOf("") }
    var modelState by remember { mutableStateOf("") }
    var voiceState by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val defaultUrl = stringResource(id = R.string.settings_placeholder_url) // Hole default URL aus string (optional)
    // Oder behalte es hartcodiert, wenn es eine feste API ist:
    // val defaultUrl = "https://api.openai.com/v1/audio/speech"
    val defaultModel = stringResource(id = R.string.settings_placeholder_model).substringAfter("e.g., ") // Hole default aus string
    val defaultVoice = stringResource(id = R.string.settings_placeholder_voice).substringAfter("e.g., ") // Hole default aus string


    LaunchedEffect(Unit) {
        isLoading = true
        context.settingsDataStore.data.firstOrNull()?.let { prefs ->
            urlState = prefs[PrefKeys.BACKEND_URL] ?: defaultUrl // Verwende defaultUrl
            apiKeyState = prefs[PrefKeys.API_KEY] ?: ""
            modelState = prefs[PrefKeys.TTS_MODEL] ?: defaultModel
            voiceState = prefs[PrefKeys.TTS_VOICE] ?: defaultVoice
            Log.d("SettingsScreen", "Initial values loaded from DataStore.")
        } ?: run {
            urlState = defaultUrl
            modelState = defaultModel
            voiceState = defaultVoice
            Log.d("SettingsScreen", "Using default values (DataStore might be empty).")
        }
        isLoading = false
    }

    val models = listOf("tts-1", "tts-1-hd")
    val voices = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) }, // Geändert
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.settings_back_description)) // Geändert
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(id = R.string.settings_instruction), // Geändert
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = urlState,
                    onValueChange = { urlState = it },
                    label = { Text(stringResource(id = R.string.settings_label_url)) }, // Geändert
                    placeholder = { Text(urlState.ifBlank { defaultUrl }) }, // Zeige Default oder aktuellen Wert als Platzhalter
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apiKeyState,
                    onValueChange = { apiKeyState = it },
                    label = { Text(stringResource(id = R.string.settings_label_api_key)) }, // Geändert
                    placeholder = { Text(stringResource(id = R.string.settings_placeholder_api_key)) }, // Geändert
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = modelState,
                    onValueChange = { modelState = it },
                    label = { Text(stringResource(id = R.string.settings_label_model)) }, // Geändert
                    placeholder = { Text(modelState.ifBlank { defaultModel }) }, // Geändert
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = voiceState,
                    onValueChange = { voiceState = it },
                    label = { Text(stringResource(id = R.string.settings_label_voice)) }, // Geändert
                    placeholder = { Text(voiceState.ifBlank { defaultVoice }) }, // Geändert
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val validationErrorMsg = context.getString(R.string.settings_snackbar_validation_error)
                            val savedMsg = context.getString(R.string.settings_snackbar_saved)
                            val errorMsg = context.getString(R.string.settings_snackbar_save_error)

                            try {
                                val urlToSave = urlState.trim()
                                val modelToSave = modelState.trim()
                                val voiceToSave = voiceState.trim()

                                if (urlToSave.isBlank() || modelToSave.isBlank() || voiceToSave.isBlank()) {
                                    snackbarHostState.showSnackbar(validationErrorMsg) // Geändert
                                    return@launch
                                }

                                context.settingsDataStore.edit { settings ->
                                    settings[PrefKeys.BACKEND_URL] = urlToSave
                                    settings[PrefKeys.API_KEY] = apiKeyState
                                    settings[PrefKeys.TTS_MODEL] = modelToSave
                                    settings[PrefKeys.TTS_VOICE] = voiceToSave
                                }
                                Log.i("SettingsScreen", "Settings saved!")
                                snackbarHostState.showSnackbar(savedMsg) // Geändert
                            } catch (e: Exception) {
                                Log.e("SettingsScreen", "Failed to save settings", e)
                                snackbarHostState.showSnackbar(errorMsg) // Geändert
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.settings_button_save)) // Geändert
                }
            } // Ende Column
        } // Ende else (isLoading)
    } // Ende Scaffold
} // Ende SettingsScreen

// Preview bleibt auskommentiert oder muss angepasst werden, um context/strings zu nutzen
//@Preview(showBackground = true, widthDp = 360, heightDp = 640)
//@Composable
//fun SettingsScreenPreview() { ... }