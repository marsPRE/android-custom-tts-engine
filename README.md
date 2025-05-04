An Android Text-to-Speech (TTS) engine that connects to configurable OpenAI-compatible API backends (those supporting the /v1/audio/speech endpoint format).

This allows you to use custom, self-hosted, or alternative cloud TTS voices (like those powered by Piper, CoquiTTS, local LLMs, or other services offering a compatible API) as a standard system-wide TTS engine on your Android device.

Features:

Integrates as a standard Android TTS Engine (selectable in device Settings).
Connects to your specified backend URL.
Authenticates using your provided API Key.
Basic voice mapping based on requested language.
(Planned: Settings UI for easier configuration of URL, API Key, and voice mappings).
Current Status:

The core functionality for connecting to an OpenAI-compatible backend (tested with the official OpenAI API) and synthesizing speech via the Android TextToSpeechService framework is implemented.

Configuration:

Currently, the backend URL and API Key need to be set as constants directly within the DummyTtsService.kt source code. Modify the BACKEND_URL and API_KEY constants before building. Remember to handle your API key securely and avoid committing it to version control.
