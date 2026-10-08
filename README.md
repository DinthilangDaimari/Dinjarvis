# J.A.R.V.I.S. AI Personal Assistant for Android

A futuristic, voice-powered AI personal assistant application inspired by Tony Stark's J.A.R.V.I.S. Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Gemini AI**, **Android TextToSpeech (TTS)**, **Speech Recognition**, and **Room Database**.

---

## 🌟 Key Features

### 🎙️ Natural Voice Engine & Intelligence Core
- **Speech-to-Text (STT)**: Real-time microphone listening with audio wave responsiveness.
- **Text-to-Speech (TTS)**: Refined British assistant voice (`Locale.UK`, 0.95x pitch, 1.02x cadence) with multi-accent support (UK, US, AU), customizable sliders, and live 16-bar soundwave equalizer visualization.
- **Gemini 3.5 Flash Reasoning**: Understands complex multi-step directives and converts them into structured tool executions.
- **Zero-Latency Offline Fallback**: Instant local heuristic parser ensuring immediate command handling even when offline.

---

### 🛡️ Five Core Operational Domains

1. **Calendar & Schedule Management**
   - View, schedule, and cancel briefings and meetings.
   - Categorized by *Stark Industries*, *Lab*, *Defense*, and *Personal*.
   - Voice commands: *"Schedule meeting with Pepper for 3 PM tomorrow"*, *"What's on my schedule today?"*.

2. **Email & Communication Transmissions**
   - Priority inbox with unread counters and starring.
   - Transmission composer and modal viewer with *"Reply via Jarvis"*.
   - Voice commands: *"Read my unread emails"*, *"Send an email to Pepper regarding clean energy report"*.

3. **Cellular & Phone Relays**
   - Priority speed dial contacts (*Pepper Potts, Col. James Rhodes, Director Nick Fury, Happy Hogan, Peter Parker*).
   - Real Android dialer integration via `Intent.ACTION_DIAL`.
   - Communication logs tracking incoming, outgoing, and missed calls.
   - Voice commands: *"Call Colonel Rhodes"*, *"Dial 555-0199"*.

4. **Application Manager & System Launcher**
   - Quick launch essential system apps (*YouTube, Google Maps, Chrome, Camera, Calculator, Clock & Alarms, Settings*).
   - Voice commands: *"Open YouTube"*, *"Launch camera"*, *"Open calculator"*.

5. **Smart Home Automation & IoT Relays**
   - Room-by-room controls (*Living Room, Workshop Lab, Penthouse, Arc Reactor Core, Perimeter*).
   - Interactive sliders and switches for lights (0–100% brightness), climate thermostats (60–85°F), biometric air-locks, and holographic defense shields.
   - Tactical automated protocols (*Protocol Daybreak, Protocol Mark Calibration, Protocol Night Watch, Protocol House Party*).
   - Voice commands: *"Turn on the workshop lights"*, *"Set thermostat to 72 degrees"*, *"Lockdown the tower"*.

---

## 🚀 Tech Stack

- **UI**: Jetpack Compose, Material 3, custom Canvas Arc Reactor animation
- **Language**: Kotlin 2.2.10
- **Database**: Room Database with KSP (`room-ktx`, `room-runtime`)
- **Networking**: Retrofit, OkHttp 4, Moshi
- **AI**: Google Gemini API (`gemini-3.5-flash`)
- **Voice**: Android SpeechRecognizer & TextToSpeech (`android.speech.tts.TextToSpeech`)
- **Typography**: Orbitron and Rajdhani Google Fonts bundled locally

---

## 🛠️ Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 36 (Minimum SDK: 24)

### Setup
1. Clone the repository:
   ```bash
   git clone <your-github-repo-url>
   cd jarvis-android
   ```
2. Open the project in **Android Studio**.
3. (Optional) Configure your Gemini API key in `.env`:
   ```bash
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY
   ```
4. Build and run on an Android device or emulator!
