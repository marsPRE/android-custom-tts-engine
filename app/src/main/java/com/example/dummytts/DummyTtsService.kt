package com.example.dummytts // <<< ANPASSEN

import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import com.example.dummytts.data.PrefKeys // <<< ANPASSEN
import com.example.dummytts.data.settingsDataStore // <<< ANPASSEN
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class DummyTtsService : TextToSpeechService() {

    companion object {
        // Logging Tag
        private const val TAG = "DummyTtsService"
    }

    // Ktor HTTP Client
    private lateinit var httpClient: HttpClient
    // Coroutine Scope für Hintergrundaufgaben
    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Scope und Client initialisieren
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            // HttpTimeout Plugin HINZUFÜGEN:
            install(HttpTimeout) {
                // Timeout für die gesamte Anfrage (Senden + Warten + Empfangen)
                // Setze diesen Wert großzügig, z.B. 60 Sekunden (60_000 ms) oder mehr
                requestTimeoutMillis = 60000

                // Timeout für den Verbindungsaufbau zum Server
                connectTimeoutMillis = 10000 // 10 Sekunden sollten reichen

                // Timeout zwischen dem Empfang von Datenpaketen (wichtig bei langsamen Antworten)
                // Setze diesen auch großzügig, z.B. 60 Sekunden
                socketTimeoutMillis = 60000
            }
            // Optional: Timeouts etc.
            // engine { requestTimeout = 30_000 }
        }
        Log.i(TAG, "Service Created, HttpClient and Scope initialized.")
    }

    override fun onDestroy() {
        Log.d(TAG, "Service Destroyed")
        // Ressourcen freigeben
        if (::httpClient.isInitialized) {
            httpClient.close()
            Log.i(TAG, "HttpClient closed.")
        }
        if (::serviceScope.isInitialized) {
            serviceScope.cancel() // Wichtig: Bricht laufende Coroutinen ab
            Log.i(TAG, "Coroutine Scope cancelled.")
        }
        super.onDestroy()
    }

    //--------------------------------------------------------------------------
    // Implementierung der abstrakten TTS-Methoden
    //--------------------------------------------------------------------------

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        Log.d(TAG, "onIsLanguageAvailable: lang=$lang, country=$country, variant=$variant")
        // Beispiel: Annahme, dass Backend Englisch, Deutsch, Spanisch unterstützt
        // TODO: An tatsächliche Backend-Fähigkeiten anpassen
        return when (lang?.lowercase()) {
            "eng" -> TextToSpeech.LANG_COUNTRY_AVAILABLE // Oder LANG_AVAILABLE
            "de" -> TextToSpeech.LANG_COUNTRY_AVAILABLE
            "es" -> TextToSpeech.LANG_COUNTRY_AVAILABLE
            else -> TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onGetLanguage(): Array<String>? {
        Log.d(TAG, "onGetLanguage called")
        // Beispiel: Gibt Englisch US als Standard zurück
        // TODO: An ausgewählte oder konfigurierte Standardsprache anpassen
        return arrayOf("eng", "USA", "")
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val result = onIsLanguageAvailable(lang, country, variant)
        Log.d(TAG, "onLoadLanguage for $lang-$country-$variant: Result=$result")
        // TODO: Hier könnte man intern die zu verwendende Stimme für die nächste Synthese wählen
        return result
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        // TODO: Implementiere Logik zum Abbrechen laufender Synthesen.
        // Das erfordert, die Coroutine-Jobs zu verwalten und `job.cancel()` aufzurufen.
        // Momentan wird eine laufende Anfrage NICHT abgebrochen.
    }

    //--------------------------------------------------------------------------
    // Haupt-Synthese-Logik
    //--------------------------------------------------------------------------

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        // Null-Checks für Request und Callback
        if (request == null || callback == null) {
            Log.e(TAG, "onSynthesizeText: Request or Callback is null")
            return
        }

        // Extrahiere Parameter aus der Anfrage
        val text = request.charSequenceText?.toString() ?: ""
        val language = request.language ?: "en"
        val country = request.country ?: "US"
        // Android Rate (100=normal) -> OpenAI Speed (1.0=normal)
        val androidRate = request.speechRate.toFloat().coerceIn(20f, 300f)
        val openAiSpeed = androidRate / 100.0f

        Log.d(TAG, "Synthesize request received: lang=$language-$country, text='${text.take(50)}...', rate=$androidRate")

        // Leeren Text nicht verarbeiten, aber Callback abschließen
        if (text.isBlank()) {
            Log.w(TAG, "Text to synthesize is blank.")
            try {
                callback.start(16000, AudioFormat.ENCODING_PCM_16BIT, 1) // Dummy Start
                callback.done()
            } catch (e: Exception) { Log.e(TAG, "Error completing callback for blank text", e)}
            return
        }

        // Starte die Coroutine für die eigentliche Arbeit
        serviceScope.launch {
            // Stelle sicher, dass der Callback noch gültig ist
            val safeCallback = callback ?: run {
                Log.w(TAG, "Callback became null before coroutine could process.")
                return@launch
            }

            try {
                // --- Einstellungen lesen ---
                Log.d(TAG, "Reading settings from DataStore...")
                val currentSettings = applicationContext.settingsDataStore.data.first()
                val backendUrl = currentSettings[PrefKeys.BACKEND_URL] ?: "" // Kein Standard hier, muss gesetzt sein
                val apiKey = currentSettings[PrefKeys.API_KEY] ?: ""
                val apiModel = currentSettings[PrefKeys.TTS_MODEL] ?: "tts-1" // Standardmodell
                val apiVoice = currentSettings[PrefKeys.TTS_VOICE] ?: "alloy" // Standardstimme
                val requestedAudioFormat = "pcm" // Fordere PCM an!

                // Prüfen, ob notwendige Einstellungen vorhanden sind
                if (backendUrl.isBlank() || apiKey.isBlank()) {
                    Log.e(TAG, "Backend URL or API Key is missing in settings.")
                    safeCallback.error(TextToSpeech.ERROR_SERVICE) // Konfigurationsfehler
                    return@launch
                }
                Log.d(TAG, "Using Settings: URL=$backendUrl, Key=******, Model=$apiModel, Voice=$apiVoice, Format=$requestedAudioFormat, Speed=$openAiSpeed")

                // --- Netzwerkanfrage ---
                Log.d(TAG, "Sending request to backend...")
                val response: HttpResponse = httpClient.post(backendUrl) {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(TtsRequestPayload(
                        model = apiModel,
                        input = text,
                        voice = apiVoice,
                        response_format = requestedAudioFormat,
                        speed = openAiSpeed
                    ))
                }
                Log.d(TAG, "Backend response status: ${response.status}")

                // --- Antwortverarbeitung ---
                if (response.status.isSuccess()) {
                    val audioBytes = response.readBytes()
                    Log.d(TAG, "Received ${audioBytes.size} audio bytes.")

                    if (audioBytes.isEmpty()) {
                        Log.w(TAG, "Received empty audio data from backend.")
                        safeCallback.error(TextToSpeech.ERROR_SYNTHESIS)
                        return@launch
                    }

                    // --- Callback starten & Audio streamen (Annahme: PCM wurde empfangen!) ---
                    // TODO: Anpassen, falls Backend anderes PCM-Format liefert oder Dekodierung nötig ist!
                    val sampleRateInHz = 24000 // Annahme für OpenAI PCM
                    val encoding = AudioFormat.ENCODING_PCM_16BIT
                    val channelCount = 1 // Mono

                    Log.d(TAG, "Calling callback.start() with Rate=$sampleRateInHz, Encoding=$encoding, Channels=$channelCount")
                    val startResult = safeCallback.start(sampleRateInHz, encoding, channelCount)
                    if (startResult == TextToSpeech.ERROR) {
                        Log.e(TAG, "Callback.start() failed!")
                        safeCallback.error(TextToSpeech.ERROR_OUTPUT)
                        return@launch
                    }

                    // Streaming
                    Log.d(TAG, "Starting to stream ${audioBytes.size} bytes to callback...")
                    val audioChunkSize = 8192 // Feste Chunk-Größe
                    var offset = 0
                    while (offset < audioBytes.size) {
                        val chunkSize = Math.min(audioBytes.size - offset, audioChunkSize)
                        val audioAvailableResult = safeCallback.audioAvailable(audioBytes, offset, chunkSize)
                        if (audioAvailableResult == TextToSpeech.ERROR) {
                            Log.e(TAG, "Callback.audioAvailable() failed!")
                            safeCallback.error(TextToSpeech.ERROR_OUTPUT)
                            return@launch
                        }
                        offset += chunkSize
                    }
                    Log.d(TAG, "Finished streaming audio data.")

                    // --- Synthese abschließen ---
                    Log.d(TAG, "Calling callback.done()")
                    val doneResult = safeCallback.done()
                    if (doneResult == TextToSpeech.ERROR) {
                        Log.e(TAG, "Callback.done() failed!")
                    } else {
                        Log.i(TAG, "Synthesis completed successfully for the request.")
                    }

                } else {
                    // Fehler bei der Backend-Antwort
                    val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Could not read error body: ${e.message}" }
                    Log.e(TAG, "Backend request failed: Status=${response.status}, Body='$errorBody'")
                    safeCallback.error(TextToSpeech.ERROR_NETWORK)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during network request or processing", e)

                // Versuche, spezifischere Fehlercodes zu setzen
                val errorCode = when (e) {
                    // Ktor: Fehler bei der Client-Anfrage (z.B. ungültige URL, DNS-Problem)
                    is io.ktor.client.plugins.ClientRequestException -> TextToSpeech.ERROR_NETWORK
                    // Ktor: Fehler bei der Server-Antwort (z.B. 5xx Serverfehler)
                    is io.ktor.client.plugins.ServerResponseException -> TextToSpeech.ERROR_NETWORK // Oder ERROR_SERVICE?
                    // Ktor: Fehler beim Umleiten
                    is io.ktor.client.plugins.RedirectResponseException -> TextToSpeech.ERROR_NETWORK
                    // Allgemeine Netzwerkprobleme (z.B. keine Verbindung, DNS-Problem wie zuvor)
                    is java.net.UnknownHostException -> TextToSpeech.ERROR_NETWORK
                    is java.net.ConnectException -> TextToSpeech.ERROR_NETWORK_TIMEOUT // Oder ERROR_NETWORK
                    // Timeout beim Socket
                    is java.net.SocketTimeoutException -> TextToSpeech.ERROR_NETWORK_TIMEOUT
                    // Allgemeine IO-Fehler (können auch Netzwerkprobleme sein)
                    is java.io.IOException -> TextToSpeech.ERROR_NETWORK // Fängt diverse Netzwerk-IO-Probleme
                    // Fehler bei der JSON-Verarbeitung
                    is kotlinx.serialization.SerializationException -> TextToSpeech.ERROR_INVALID_REQUEST
                    // Sonstige Fehler
                    else -> TextToSpeech.ERROR_SERVICE
                }
                Log.w(TAG, "Reporting error code: $errorCode")

                try {
                    safeCallback.error(errorCode)
                } catch (callbackError: Exception) {
                    Log.e(TAG, "Error reporting error code $errorCode to callback", callbackError)
                }
            } // Ende try-catch
        } // Ende serviceScope.launch

        Log.d(TAG, "onSynthesizeText finished launching coroutine.")
    } // Ende onSynthesizeText

} // Ende DummyTtsService Klasse