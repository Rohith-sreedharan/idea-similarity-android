# Idea Similarity Android App

Android client for the Idea Similarity system, built with Kotlin + Jetpack Compose.

The app connects to the FastAPI backend and supports:

- Login and signup
- Manuscript title scan and similarity analysis
- Source breakdown and PDF report export
- Settings pages with inner routes
- Unsplash profile icon selection with persistent backend save

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + Gson
- Coil (remote profile image loading)

## Project Structure

```
idea-similarity-android/
├── app/
│   ├── src/main/java/com/example/ideasimilarityapp/
│   │   ├── MainActivity.kt
│   │   └── ApiService.kt
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 17
- Android SDK installed
- A running backend at port `8005`

## Backend Base URL

The app currently points to this backend URL in `MainActivity.kt`:

`http://10.180.5.246:8005/`

Update this to your machine IP if needed.

## Build and Run

From project root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Or run directly from Android Studio.

## Main Screens

1. Splash
2. Login
3. Signup
4. Home (Search / Scan)
5. Result (Library view)
6. Settings
7. Settings inner pages:
	- Account Settings
	- Subscription
	- Notification Preferences
	- Help and Support

## Key Integrations

- `POST /auth/login`
- `POST /auth/signup`
- `POST /check`
- `GET /settings/summary/{user_id}`
- `GET /settings/account/{user_id}`
- `GET /settings/subscription/{user_id}`
- `GET /settings/notifications/{user_id}`
- `GET /settings/help/{user_id}`
- `GET /settings/profile-icons`
- `PUT /settings/account/profile-image/{user_id}`

## Notes

- PDF reports are generated on-device from the result page.
- Profile icon updates are saved through backend APIs and persist across app restarts.
- If build warnings mention deprecated icons, app functionality remains unaffected.
