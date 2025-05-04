package com.example.dummytts // Passe diesen Paketnamen an!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// Importiere notwendige Compose-Funktionen und dein Theme/SettingsScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings // Icon für Einstellungen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Passe diese Imports an deine Projektstruktur an!
// Wenn SettingsScreen.kt in com.example.dummytts.ui liegt:
import com.example.dummytts.ui.SettingsScreen
// Wenn SettingsScreen.kt direkt in com.example.dummytts liegt:
// import com.example.dummytts.SettingsScreen

// Importiere dein App-Theme (passe ggf. den Pfad an)
import com.example.dummytts.ui.theme.DummyTTSTheme // Annahme, dass dein Theme so heißt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class) // Für Scaffold, TopAppBar etc.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setze den Compose-Inhalt für diese Activity
        setContent {
            // Wende dein App-Theme an
            DummyTTSTheme {

                // Zustandsvariable, die steuert, welcher Bildschirm angezeigt wird
                // false = Hauptbildschirm, true = Einstellungsbildschirm
                var showSettings by remember { mutableStateOf(false) }

                // Bedingte Anzeige basierend auf dem Zustand 'showSettings'
                if (showSettings) {
                    // Zeige den Einstellungsbildschirm an
                    SettingsScreen(
                        // Übergib eine Funktion, die aufgerufen wird, wenn der Benutzer zurück möchte
                        onNavigateBack = {
                            // Setze den Zustand zurück, um zum Hauptbildschirm zu wechseln
                            showSettings = false
                        }
                    )
                } else {
                    // Zeige den Hauptbildschirm der App an
                    Scaffold(
                        topBar = {
                            // Obere App-Leiste
                            TopAppBar(
                                title = { Text("Dummy TTS Main") }, // Titel der App
                                actions = {
                                    // Aktions-Icon rechts in der Leiste
                                    // IconButton, um zu den Einstellungen zu wechseln
                                    IconButton(onClick = { showSettings = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings, // Einstellungs-Icon
                                            contentDescription = "Settings" // Beschreibung für Barrierefreiheit
                                        )
                                    }
                                }
                            )
                        }
                    ) { paddingValues -> // Inner Padding vom Scaffold berücksichtigen
                        // Hauptinhalt des Bildschirms
                        Column(
                            modifier = Modifier
                                .fillMaxSize() // Fülle den gesamten verfügbaren Platz
                                .padding(paddingValues) // Wende Padding vom Scaffold an
                                .padding(16.dp), // Füge eigenes Padding hinzu
                            verticalArrangement = Arrangement.Center, // Zentriere vertikal
                            horizontalAlignment = Alignment.CenterHorizontally // Zentriere horizontal
                        ) {
                            Text("Hauptbildschirm der App (Compose)")
                            Spacer(modifier = Modifier.height(20.dp)) // Abstand
                            Text("TTS Engine ist im Hintergrund aktiv.")
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Klicke oben rechts auf das Zahnrad für die Einstellungen.")
                        }
                    } // Ende Scaffold (Hauptinhalt)
                } // Ende else (Hauptinhalt)
            } // Ende Theme
        } // Ende setContent
    } // Ende onCreate
}

// Optionale Preview-Funktion für die Android Studio Vorschau
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Wende das Theme auch in der Vorschau an
    DummyTTSTheme {
        // Zeige nur den Hauptteil des Scaffolds für die Vorschau an
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dummy TTS Main") },
                    actions = {
                        IconButton(onClick = { /* Vorschau: keine Aktion */ }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                Text("Hauptbildschirm der App (Compose)")
                Spacer(modifier = Modifier.height(20.dp))
                Text("TTS Engine ist im Hintergrund aktiv.")
            }
        }
    }
}