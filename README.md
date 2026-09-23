# Upper Mustang Weather Alert App

An offline-first Android application designed for high-altitude conditions in Lo Manthang, Upper Mustang (Nepal). 

In remote mountain areas, mobile network connections drop frequently, freezing conditions make typing difficult, and severe weather can be life-threatening. This application provides cached forecasts, automated storm warnings, and Nepali voice synthesis for hands-free accessibility.

---

## What It Does

* **Offline-First Caching:** Caches weather responses locally so the latest forecast remains readable immediately, even when completely offline.
* **Safety Alert Engine:** Decoupled business logic (`AlertEngine`) that monitors wind speed (> 17 m/s) and heavy snowfall (> 150 mm) to trigger visual safety banners and warnings.
* **Nepali Text-To-Speech (TTS):** Uses Android’s TTS engine in Nepali (`ne-NP`) to read weather reports and warnings aloud for users outdoors or wearing gloves.
* **Voice Search:** Integrated Google Voice Recogniser for hands-free location lookup.
* **Safety Habit Rewards:** Awards engagement points for checking forecasts before travelling mountain routes.
* **Secure API Configuration:** Secrets are externalised to `local.properties` and injected via Gradle `BuildConfig` to prevent credential exposure.

---

## Tech Stack

* **Language:** Java
* **Platform:** Android SDK (API 24 to 35)
* **Networking:** Volley HTTP library
* **Audio & Speech:** Android Text-To-Speech API (`ne-NP`), SpeechRecognizer
* **Testing:** JUnit 4
* **Backend / Auth:** Firebase Auth (Anonymous authentication)
* **API:** OpenWeatherMap API

---

## Unit Testing

To verify safety thresholds independently of the Android UI, business logic is isolated in `AlertEngine.java` and tested using JUnit:

* `AlertEngineTest.java`: Tests boundary conditions for high winds, heavy snow, and normal operations.

Run tests via the terminal:
```bash
./gradlew test
