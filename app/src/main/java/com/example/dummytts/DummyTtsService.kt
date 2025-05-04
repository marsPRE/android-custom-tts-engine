package com.example.dummytts

import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import android.media.AudioFormat

// Ktor Imports hinzufügen
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


// Ktor HTTP Client Request/Response Imports hinzufügen
import io.ktor.client.request.* // Für post, header, bearerAuth, setBody, contentType
import io.ktor.client.statement.* // Für HttpResponse, bodyAsChannel
import io.ktor.client.statement.readBytes // Import für die Korrektur
import io.ktor.http.* // Für ContentType
import io.ktor.utils.io.readRemaining // Für das Lesen des Response Body


// Coroutine Imports hinzufügen
import kotlinx.coroutines.* // Importiere CoroutineScope, SupervisorJob, Dispatchers, cancel



class DummyTtsService : TextToSpeechService() {

    companion object {
        private const val TAG = "OpenAiTtsService" // Du kannst den TAG anpassen
        // ERSETZE DIESE PLATZHALTER MIT DEINEN ECHTEN WERTEN!
        //private const val BACKEND_URL = "https://api.openai.com/v1/audio/speech"
        private const val BACKEND_URL = "http://127.0.0.1:8081/v1/audio/speech"
        private const val API_KEY = "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"

        // SICHERHEITSHINWEIS:
        // Speichere niemals echte API-Schlüssel direkt im Quellcode!
        // Dies ist nur für Testzwecke. In einer echten App müssen
        // diese Werte sicher geladen werden (z.B. aus BuildConfig,
        // verschlüsselten SharedPreferences oder über einen Server).
    }
    private lateinit var httpClient: HttpClient
    private lateinit var serviceScope: CoroutineScope
    private val TAG = "DummyTtsService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Dummy TTS Service Created")
        // HttpClient hier initialisieren
        httpClient = HttpClient(CIO) { // CIO Engine ist gut für Android
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Ignoriert Felder im JSON, die nicht im Datenmodell sind
                    prettyPrint = false // Für Produktion auf false setzen
                    isLenient = true // Erlaubt etwas lockerere JSON-Formate
                })
            }
            // Optional: Timeouts konfigurieren
            // engine {
            //    requestTimeout = 30_000 // z.B. 30 Sekunden Timeout für Anfragen
            // }
        }
        Log.i(TAG, "Ktor HttpClient initialized.")
        // Coroutine Scope hier initialisieren
        // SupervisorJob: Sorgt dafür, dass der Ausfall einer Coroutine nicht den gesamten Scope beendet
        // Dispatchers.IO: Geeignet für Netzwerk- und Festplattenoperationen
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        Log.i(TAG, "Coroutine Scope initialized.")
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        // Behaupte, nur Englisch (USA) zu unterstützen
        if ("eng" == lang && "USA" == country) {
            return TextToSpeech.LANG_COUNTRY_AVAILABLE
        }
        return TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onGetLanguage(): Array<String>? {
        // Gib die "Standard"-Sprache zurück
        return arrayOf("eng", "USA", "")
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        // Gib das Ergebnis der Verfügbarkeitsprüfung zurück
        return onIsLanguageAvailable(lang, country, variant)
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        // Hier würdest du normalerweise die Synthese abbrechen
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        if (request == null || callback == null) {
            Log.e(TAG, "Request or Callback is null")
            // Optional: callback.error() hier aufrufen, wenn callback nicht null ist
            return
        }

        // 1. Parameter aus der Anfrage extrahieren
        val language = request.language ?: "en" // Standard: Englisch
        val country = request.country ?: "US"
        val variant = request.variant ?: ""
        val text = request.charSequenceText.toString()
        // Android Rate ist 100 = normal, OpenAI Speed ist 1.0 = normal
        val androidRate = request.speechRate.toFloat().coerceIn(20f, 300f) // Sicherstellen, dass Rate in Grenzen ist
        val openAiSpeed = androidRate / 100.0f // Umrechnung
        // Android Pitch ist 100 = normal, OpenAI hat keinen direkten Pitch-Parameter
        val pitch = request.pitch

        Log.d(TAG, "Synthesize request: lang=$language-$country-$variant, text='$text', rate=$androidRate, pitch=$pitch")

        if (text.isBlank()) {
            Log.w(TAG, "Text to synthesize is blank.")
            // Wichtig: Auch bei leerem Text den Callback abschließen!
            callback.start(16000, AudioFormat.ENCODING_PCM_16BIT, 1) // Dummy-Start
            callback.done()
            return
        }

        // 2. Modell und Stimme für die API festlegen (Beispiele)
        //    TODO: Später Logik hinzufügen, um dies basierend auf language/country/variant
        //          oder einer Benutzereinstellung auszuwählen.
        val apiModel = "tts-1" // Oder "tts-1-hd"
        val apiVoice = when (language) {
            // Sehr einfaches Beispiel-Mapping
            "de" -> "alloy" // Nur als Beispiel, wähle passende Stimmen
            "es" -> "echo"
            else -> "nova" // Standardstimme
        }

        // 3. Gewünschtes Audioformat vom Backend festlegen
        //    "pcm" ist ideal für die direkte Weitergabe an den Callback.
        //    Andernfalls (z.B. "mp3") ist Dekodierung nötig (siehe späterer Schritt).
        val requestedAudioFormat = "pcm" // ÄNDERN, falls dein Backend kein PCM liefert

        Log.d(TAG, "Using Backend: Model=$apiModel, Voice=$apiVoice, Format=$requestedAudioFormat, Speed=$openAiSpeed")

        // --- HIER STARTET DER NETZWERKAUFRUF ---
        // Starte die Coroutine im IO Dispatcher unseres Service Scopes
        serviceScope.launch {
            // Zugriff auf den Callback innerhalb der Coroutine sicherstellen
            // (Callback könnte null sein, wenn der Service schnell gestoppt wird)
            val safeCallback = callback ?: run {
                Log.w(TAG, "Callback became null before coroutine could process.")
                return@launch // Coroutine beenden, wenn kein Callback mehr da ist
            }

            try {
                Log.d(TAG, "Coroutine launched. Sending request to backend: $BACKEND_URL")

                // HTTP POST Anfrage erstellen und senden
                val response: HttpResponse = httpClient.post(BACKEND_URL) {
                    // Authentifizierung hinzufügen (Bearer Token)
                    header(HttpHeaders.Authorization, "Bearer $API_KEY")
                    // Setze den Content-Type Header auf application/json
                    contentType(ContentType.Application.Json)
                    // Erstelle das Payload-Objekt und setze es als Request Body
                    setBody(TtsRequestPayload(
                        model = apiModel,
                        input = text,
                        voice = apiVoice,
                        response_format = requestedAudioFormat,
                        speed = openAiSpeed
                    ))
                }

                Log.d(TAG, "Backend response status: ${response.status}")

                // --- VERARBEITUNG DER ANTWORT ---
                if (response.status.isSuccess()) {
                    // Antwort war erfolgreich (z.B. 200 OK)

                    // Audiodaten als Byte-Array aus dem Response Body lesen
                    val audioBytes = response.readBytes()
                    Log.d(TAG, "Received ${audioBytes.size} audio bytes.")

                    // Prüfen, ob Daten empfangen wurden
                    if (audioBytes.isEmpty()) {
                        Log.w(TAG, "Received empty audio data from backend.")
                        safeCallback.error(TextToSpeech.ERROR_SYNTHESIS)
                        return@launch // Coroutine hier beenden
                    }

// 1. Audioformat-Parameter bestimmen
                    //    Diese MÜSSEN dem Format in 'audioBytes' entsprechen!
                    val sampleRateInHz: Int
                    val encoding: Int
                    val channelCount: Int

                    // Szenario 1: Wir haben PCM vom Backend angefordert und erhalten
                    if (requestedAudioFormat == "pcm") {
                        // Annahmen für OpenAI PCM (überprüfe die Doku deines Backends!)
                        sampleRateInHz = 24000 // Typisch für OpenAI PCM
                        encoding = AudioFormat.ENCODING_PCM_16BIT // Standard PCM Format
                        channelCount = 1 // Normalerweise Mono
                        Log.d(TAG, "Audio format is PCM: $sampleRateInHz Hz, 16-bit, Mono")
                    }
                    // Szenario 2: Wir haben MP3 angefordert (oder ein anderes komprimiertes Format)
                    else if (requestedAudioFormat == "mp3") {
                        // << HIER IST DIE KOMPLEXITÄT >>
                        // Wenn du MP3 direkt an den Callback senden willst (nicht empfohlen, aber möglich),
                        // müsstest du AudioFormat.ENCODING_MP3 verwenden. ABER: Samplerate und Kanalzahl
                        // sind aus dem MP3-Header zu lesen oder bekannt/festgelegt. Das ist fehleranfällig.
                        //
                        // *** EMPFEHLUNG: Dekodiere MP3 zu PCM, BEVOR du callback.start() aufrufst! ***
                        // Wenn du dekodierst, würdest du hier die Parameter des *dekodierten* PCM setzen.
                        // Beispiel (wenn zu 16kHz PCM dekodiert wurde):
                        // sampleRateInHz = 16000
                        // encoding = AudioFormat.ENCODING_PCM_16BIT
                        // channelCount = 1
                        //
                        // FÜR DIESES BEISPIEL GEHEN WIR DAVON AUS, DASS WIR PCM ANGEFORDERT HABEN (siehe oben)
                        // Falls du MP3 angefordert hast, melde vorerst einen Fehler:
                        Log.e(TAG, "Received format '$requestedAudioFormat' requires decoding to PCM before calling callback.start(). Please request 'pcm' from backend or implement a decoder.")
                        safeCallback.error(TextToSpeech.ERROR_SERVICE) // Fehler: Falsches Format intern
                        return@launch
                    }
                    // Szenario 3: Andere Formate (Opus, AAC, ...)
                    else {
                        // Ähnlich wie MP3: Dekodierung zu PCM ist der robuste Weg.
                        Log.e(TAG, "Unsupported audio format '$requestedAudioFormat' received. Decoding to PCM required.")
                        safeCallback.error(TextToSpeech.ERROR_SERVICE)
                        return@launch
                    }

                    // 2. Callback starten
                    Log.d(TAG, "Calling callback.start() with Rate=$sampleRateInHz, Encoding=$encoding, Channels=$channelCount")
                    val startResult = safeCallback.start(sampleRateInHz, encoding, channelCount)

                    // 3. Ergebnis von start() prüfen
                    if (startResult == TextToSpeech.ERROR) {
                        Log.e(TAG, "Callback.start() failed!")
                        safeCallback.error(TextToSpeech.ERROR_OUTPUT) // Fehler beim Initialisieren der Audioausgabe
                        return@launch // Coroutine beenden
                    }

                    // --- HIER STARTET DAS STREAMING DER AUDIOBYTES ---
                    Log.d(TAG, "Starting to stream ${audioBytes.size} bytes to callback...")

                    // Maximale Größe eines Audio-Chunks holen, den der Callback akzeptiert

                    val audioChunkSize = 8192

                    var offset = 0
                    while (offset < audioBytes.size) {
                        // Berechne die Größe des nächsten Chunks
                        val chunkSize = Math.min(audioBytes.size - offset, audioChunkSize)

                        // Sende den Chunk an den Callback
                        val audioAvailableResult = safeCallback.audioAvailable(audioBytes, offset, chunkSize)

                        // Prüfe auf Fehler nach dem Senden des Chunks
                        if (audioAvailableResult == TextToSpeech.ERROR) {
                            Log.e(TAG, "Callback.audioAvailable() failed!")
                            safeCallback.error(TextToSpeech.ERROR_OUTPUT)
                            return@launch // Coroutine bei Fehler beenden
                        }

                        // Gehe zum nächsten Offset
                        offset += chunkSize
                    } // Ende while-Schleife

                    Log.d(TAG, "Finished streaming audio data.")

                    // --- HIER WIRD DIE SYNTHESE ABGESCHLOSSEN ---
                    Log.d(TAG, "Calling callback.done()")
                    val doneResult = safeCallback.done()

                    if (doneResult == TextToSpeech.ERROR) {
                        // Fehler beim Abschließen, möglicherweise wurde die Verbindung schon beendet
                        Log.e(TAG, "Callback.done() failed!")
                        // Ein explizites safeCallback.error() ist hier oft nicht mehr nötig/möglich,
                        // da done() schon der letzte Schritt ist. Logging ist hier wichtig.
                    } else {
                        Log.i(TAG, "Synthesis completed successfully for the request.")
                    }


                } else {
                    // Antwort war nicht erfolgreich (z.B. 4xx, 5xx Fehler)
                    val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Could not read error body: ${e.message}" }
                    Log.e(TAG, "Backend request failed: Status=${response.status}, Body='$errorBody'")
                    safeCallback.error(TextToSpeech.ERROR_NETWORK) // Oder spezifischer
                    // return@launch // Coroutine wird durch try-catch sowieso beendet
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
                    // Allgemeine Netzwerkprobleme (z.B. keine Verbindung)
                    is java.net.UnknownHostException -> TextToSpeech.ERROR_NETWORK_TIMEOUT // Oder ERROR_NETWORK
                    is java.net.ConnectException -> TextToSpeech.ERROR_NETWORK_TIMEOUT // Oder ERROR_NETWORK
                    is java.net.SocketTimeoutException -> TextToSpeech.ERROR_NETWORK_TIMEOUT
                    // Fehler bei der JSON-Verarbeitung (sollte selten sein bei korrekter API)
                    is kotlinx.serialization.SerializationException -> TextToSpeech.ERROR_INVALID_REQUEST // Oder ERROR_SERVICE
                    // Sonstige Fehler
                    else -> TextToSpeech.ERROR_SERVICE // Allgemeiner Service-Fehler
                }
                Log.w(TAG, "Reporting error code: $errorCode")

                // Informiere den TTS-Client über den spezifischeren Fehler
                try {
                    safeCallback.error(errorCode)
                } catch (callbackError: Exception) {
                    Log.e(TAG, "Error reporting error code $errorCode to callback", callbackError)
                }
            } // Ende try-catch
        } // Ende serviceScope.launch
    } // Ende onSynthesizeText

    override fun onDestroy() {
        Log.d(TAG, "Service Destroyed")

        // HttpClient schließen
        if (::httpClient.isInitialized) { // Prüfen, ob die Variable initialisiert wurde
            httpClient.close()
            Log.i(TAG, "Ktor HttpClient closed.")
        } else {
            Log.w(TAG, "HttpClient was not initialized before onDestroy.")
        }

        // Coroutine Scope beenden
        if (::serviceScope.isInitialized) { // Prüfen, ob die Variable initialisiert wurde
            serviceScope.cancel() // Bricht alle Coroutines in diesem Scope ab
            Log.i(TAG, "Coroutine Scope cancelled.")
        } else {
            Log.w(TAG, "ServiceScope was not initialized before onDestroy.")
        }

        super.onDestroy() // Wichtig: super.onDestroy() am Ende aufrufen
    }
}
