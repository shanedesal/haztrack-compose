# Haztrack

Android app for hazard tracking (`com.danger.haztrack`).

The product domain (hazard reports, maps, alerts) is not implemented yet. What ships today is a production-quality authentication and user-profile foundation: email and Google sign-in via Supabase Auth, password reset via a custom-scheme deep link, an editable profile stored on the self-hosted backend, and secure profile-photo uploads through that same backend.

For architecture, navigation, and layer-by-layer detail, see [`docs/docs.md`](docs/docs.md). Changes are recorded in [`docs/changelog.md`](docs/changelog.md).

---

## Current functionality

### Authentication

- **Email / password sign-in** and **registration** (first name, last name, email, password).
- **Google Sign-In** via Credential Manager. Google accounts get a “Signed in with Google” badge and can use their Google profile photo until a custom photo is uploaded. Supabase validates the Google ID token and raw nonce.
- **Forgot password** asks Supabase to send a recovery email. Opening `com.danger.haztrack://reset-password` on a device with the app installed lands in the in-app **reset password** screen. Details: [`docs/deeplinks-password-reset.md`](docs/deeplinks-password-reset.md).
- **Session persistence:** a cold start waits for Supabase Auth to restore the session, then starts at Home or Login.
- **Sign out** from Settings clears the Supabase session, the in-memory profile cache, and Credential Manager state, then returns to login.

### Profile

- Backend row for the signed-in user, loaded and saved through `GET`/`PUT` `/users/me`. The backend resolves the user from the Supabase bearer token; the app does not put a uid in the URL.
- View and edit **first name**, **last name**, **date of birth**, **gender**, and **phone number** (country-code picker with validation). Email is shown read-only.
- **Profile photo:** tap the avatar to pick an image. The app compresses it on-device and uploads through our backend (the app never talks to Cloudinary and never holds a Cloudinary secret). Removing a custom photo reverts to the Google photo if one exists, otherwise an initials avatar.
- Missing profiles are created automatically on sign-in (legacy accounts and first-time Google sign-in).

This project does **not** use Firebase (Auth, Firestore, Hosting, or CLI config).

### Post-login shell

Five destinations share a bottom navigation bar plus a docked FAB:

| Screen | Status |
|---|---|
| **Home** | Dashboard: greets the user by name/email; shortcuts to Report and My Reports |
| **Report** (FAB / Home) | Placeholder — “report a hazard” is not wired to data yet |
| **My Reports** | Placeholder — no report list yet |
| **Notifications** | Placeholder — no alerts yet |
| **Settings** | User card (opens Profile) and Sign Out |

**Profile** is reached from Settings; it is not a bottom-nav tab.

### Not in the app yet

Hazard reporting, report history, notifications, local database, and background services. Packages under `data/local`, `data/service`, and `presentation/common` are reserved for that work.

---

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable) or IntelliJ IDEA with the Android plugin
- JDK 11 or newer (the app compiles with Java 11 bytecode)
- A device or emulator with **Google Play Services** (required for Google Sign-In)
- Optional: a running backend if you need profile reads/writes or profile photos (see [Backend](#backend-optional))

---

## Secrets: contact the main developer

**Do not create your own Supabase project, invent OAuth client IDs, or invent Cloudinary / backend credentials.**

Config that contains keys and environment values is **gitignored on purpose**.

**Contact the main developer** and ask for the files and values you need. Typically that is:

| File / value | Where it goes | What it is |
|---|---|---|
| `SUPABASE_URL` and `SUPABASE_ANON_KEY` | `local.properties` → `BuildConfig` | Shared Supabase project. The anonymous key is a client-visible publishable key, not a service-role secret; still do not commit it. |
| `google-services.json` | `app/google-services.json` | Still used only so the Google Services Gradle plugin can generate `R.string.default_web_client_id` for Credential Manager. It is **not** Firebase Auth or Firestore config for this app. Obtain the team file; do not create a Firebase project. |
| Image-upload / profile backend URL | `local.properties` (`BACKEND_BASE_URL`) | Not bundled as Cloudinary secrets; the app only knows a base URL. |

Never commit these files. Never paste their contents into issues, chat, or pull requests.

`local.properties` is also gitignored. Android Studio writes `sdk.dir` there when you open the project; that path is machine-specific and is not a shared secret.

---

## Project setup

1. **Clone the repository** and open it in Android Studio.

2. **Activate git hooks** (lint/Detekt on commit, commit-message format):

   ```bash
   git config core.hooksPath .githooks
   ```

3. **Get secrets from the main developer** (see above). Add to `local.properties`:

   ```properties
   SUPABASE_URL=https://YOUR_PROJECT.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```

   Place `google-services.json` at:

   ```text
   app/google-services.json
   ```

   Gradle still applies the Google Services plugin for the OAuth web client id.

4. **Register your debug signing certificate** so Google Sign-In works on your machine. Each developer’s debug keystore has a different SHA-1. From the project root:

   ```bash
   ./gradlew signingReport
   ```

   Copy the `SHA1` under the **debug** variant. Send it to the **main developer** so they can add it to the Google Cloud OAuth Android client used by Credential Manager. Do not rotate client IDs yourself unless they ask you to.

5. **Sync Gradle** and build:

   ```bash
   ./gradlew assembleDebug
   ```

6. **Run** on a Play Services emulator or a physical device.

Static analysis used in this repo (run when you are checking a change, not required just to launch):

```bash
./gradlew detekt
```

---

## Backend (optional)

Profile fields and photo changes call the self-hosted API described in [`docs/backend-image-upload-spec.md`](docs/backend-image-upload-spec.md) (uploads) and [`docs/docs.md`](docs/docs.md) (`GET`/`PUT` `/users/me`). Auth screens still work without a backend; loading or saving a profile and changing a photo will fail until the service is running.

The base URL is **not hardcoded**. Gradle reads `BACKEND_BASE_URL` from `local.properties` and exposes it as `BuildConfig.BACKEND_BASE_URL`. If the property is omitted, the default is `http://10.0.2.2:4000/api/v1/` (Android emulator → host loopback).

Example `local.properties` entries (in addition to `sdk.dir` and Supabase keys):

```properties
BACKEND_BASE_URL=http://10.0.2.2:4000/api/v1/
```

Debug builds allow cleartext HTTP only to `10.0.2.2`, `localhost`, and `127.0.0.1`. Release builds do not. A physical device typically needs `adb reverse` plus `http://127.0.0.1:...` or a TLS tunnel. **Ask the main developer** how the service is run and which URL to use; do not put Cloudinary API secrets or a Supabase service-role key in the Android app.

---

## Tech stack (short)

Kotlin, Jetpack Compose, Material 3, MVVM, Hilt, Navigation Compose, Supabase Auth, Retrofit/OkHttp/Moshi for the backend (profile + uploads), Coil for images. Versions live in `gradle/libs.versions.toml`.

Min SDK 24, target SDK 36.

---

## Further documentation

| Doc | Contents |
|---|---|
| [`docs/docs.md`](docs/docs.md) | Architecture, MVVM, screens, auth/profile flows, Hilt, setup notes |
| [`docs/deeplinks-password-reset.md`](docs/deeplinks-password-reset.md) | Password-reset custom-scheme deep links |
| [`docs/backend-image-upload-spec.md`](docs/backend-image-upload-spec.md) | Upload API the Android client expects (Supabase JWT auth) |
| [`docs/changelog.md`](docs/changelog.md) | Feature and fix history |
