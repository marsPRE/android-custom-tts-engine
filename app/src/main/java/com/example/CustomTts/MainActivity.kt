package com.example.CustomTts // Passe Paketnamen an

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// Importiere notwendige Compose-Funktionen und dein Theme/SettingsScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // <-- Wichtiger Import!
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Passe diese Imports an deine Projektstruktur an!
import com.example.CustomTts.R // <-- Wichtiger Import!
import com.example.CustomTts.ui.SettingsScreen // Oder wo auch immer SettingsScreen liegt
import com.example.CustomTts.ui.theme.DummyTTSTheme // Dein Compose Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DummyTTSTheme {

                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    // Hauptbildschirm mit String-Ressourcen
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(id = R.string.main_title)) }, // Geändert
                                actions = {
                                    IconButton(onClick = { showSettings = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = stringResource(id = R.string.main_settings_action_description) // Geändert
                                        )
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(id = R.string.main_screen_text_1)) // Geändert
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(id = R.string.main_screen_text_2)) // Geändert
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(id = R.string.main_screen_text_3)) // Geändert
                        }
                    } // Ende Scaffold (Hauptinhalt)
                } // Ende else (Hauptinhalt)
            } // Ende Theme
        } // Ende setContent
    } // Ende onCreate
}

// Preview mit String-Ressourcen
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DummyTTSTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.main_title)) }, // Geändert
                    actions = {
                        IconButton(onClick = { /* Vorschau: keine Aktion */ }) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.main_settings_action_description)) // Geändert
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(id = R.string.main_screen_text_1)) // Geändert
                Spacer(modifier = Modifier.height(20.dp))
                Text(stringResource(id = R.string.main_screen_text_2)) // Geändert
            }
        }
    }
}