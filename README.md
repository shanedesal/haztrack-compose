# Haztrack

Android app for hazard tracking (`com.danger.haztrack`).

The product domain (hazard reports, maps, alerts) is not implemented yet. What ships today is a production-quality authentication and user-profile foundation: email and Google sign-in, password reset via verified App Links, a Firestore-backed editable profile, and secure profile-photo uploads through a self-hosted backend.

For architecture, navigation, and layer-by-layer detail, see [`docs/docs.md`](docs/docs.md). Changes are recorded in [`docs/changelog.md`](docs/changelog.md).

---

## Current functionality

### Authentication

- **Email / password sign-in** and **registration** (first name, last name, email, password).
- **Google Sign-In** via Credential Manager. Google accounts get a “Signed in with Google” badge and can use their Google profile photo until a custom photo is uploaded.
- **Forgot password** sends a Firebase reset email. Opening the link on a device with the app installed lands in an in-app **reset password** screen (verified Android App Link). Opening it in a browser uses the Hosting fallback page.
- **Session persistence:** a cold start resumes a signed-in session when Firebase still has a user; otherwise the login graph is shown.
- **Sign out** from Settings clears the Firebase session and returns to login.

### Profile

- Firestore document at `users/{uid}` (the user can only read/write their own document).
- View and edit **first name**, **last name**, **date of birth**, **gender**, and **phone number** (country-code picker with validation). Email is shown read-only.
- **Profile photo:** tap the avatar to pick an image. The app compresses it on-device and uploads through our backend (the app never talks to Cloudinary and never holds a Cloudinary secret). Removing a custom photo reverts to the Google photo if one exists, otherwise an initials avatar.
- Missing profile documents are created automatically on sign-in (legacy accounts and first-time Google sign-in).

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
- [Node.js](https://nodejs.org/) if you will use the Firebase CLI (hosting / Firestore rules)
- Optional: a running image-upload backend if you need to change profile photos (see [Profile photo backend](#profile-photo-backend-optional))

---

## Secrets: contact the main developer

**Do not create your own Firebase project, download a random `google-services.json`, or invent Cloudinary / backend credentials.**

This repo is wired to the shared Firebase project (`haztrack-62a3c`). Config files that contain API keys, OAuth client IDs, and other environment values are **gitignored on purpose**.

**Contact the main developer** and ask for the files you need. Typically that is:

| File | Where it goes | What it is |
|---|---|---|
| `google-services.json` | `app/google-services.json` | Firebase Android app config (API keys, project ID, OAuth client IDs). The Google Services Gradle plugin reads this at build time and generates resources such as `R.string.default_web_client_id` for Google Sign-In. |
| Image-upload backend URL / access (if you need photo upload) | `local.properties` (`BACKEND_BASE_URL`) and whatever the backend itself requires | Not bundled in the Android app as Cloudinary secrets; the app only knows a base URL. |

Never commit these files. Never paste their contents into issues, chat, or pull requests. If you already generated a local copy, delete it and use the files the main developer provides so you hit the same Auth, Firestore, and Hosting project as everyone else.

`local.properties` is also gitignored. Android Studio writes `sdk.dir` there when you open the project; that path is machine-specific and is not a shared secret.

---

## Project setup

1. **Clone the repository** and open it in Android Studio.

2. **Activate git hooks** (lint/Detekt on commit, commit-message format):

   ```bash
   git config core.hooksPath .githooks
   ```

3. **Get secrets from the main developer** (see above) and place `google-services.json` at:

   ```text
   app/google-services.json
   ```

   Gradle will fail to apply the Google Services plugin without this file.

4. **Register your debug signing certificate** so Google Sign-In works on your machine. Each developer’s debug keystore has a different SHA-1. From the project root:

   ```bash
   ./gradlew signingReport
   ```

   Copy the `SHA1` under the **debug** variant. Send it to the **main developer** so they can add it in Firebase Console → Project settings → Your apps → Android app → Add fingerprint. Do not add fingerprints or change OAuth clients yourself unless they ask you to.

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

## Firebase setup

The Android app talks to **Firebase Authentication** and **Cloud Firestore**. Password-reset emails use **Firebase Hosting** as the HTTPS domain for Android App Links. Repo files that describe that project:

| Path | Role |
|---|---|
| `.firebaserc` | Selects the default CLI project (`haztrack-62a3c`) |
| `firebase.json` | Hosting (`public/`) and Firestore rules/indexes paths |
| `firestore.rules` | Users may only read/write `users/{uid}` for their own uid |
| `public/` | Hosting site: App Links `assetlinks.json`, browser reset page |

You do **not** need the Firebase CLI to compile or run the Android app, as long as `google-services.json` is in place. Use the CLI when you deploy Hosting or Firestore rules, or when inspecting the linked project.

### Install and log in (Firebase CLI)

```bash
npm install -g firebase-tools
firebase login
```

`firebase login` opens a browser, signs you in with a Google account that has access to the Firebase project, and stores a refresh token on your machine (not in this repo).

Confirm you can see the project:

```bash
firebase projects:list
```

From the repository root, the project is already selected via `.firebaserc`. Check it with:

```bash
firebase use
```

If the CLI has no default, set it (only if the main developer has granted you access to this project):

```bash
firebase use haztrack-62a3c
```

If `projects:list` is empty or `use` fails, you do not have IAM access. **Contact the main developer** — do not create a second Firebase project to work around that.

### What the CLI is for in this repo

Deploy **Firestore security rules** (needed if rules on the server are older than `firestore.rules` in git):

```bash
firebase deploy --only firestore:rules
```

Deploy **Hosting** (password-reset browser page and `.well-known/assetlinks.json`):

```bash
firebase deploy --only hosting
```

These deploys change shared production/dev Firebase resources. Do not run them unless the main developer has asked you to.

The CLI does **not** generate `google-services.json`. That file is downloaded from Firebase Console → Project settings → Your apps → Android app, and in this team it is **distributed by the main developer**, not fetched ad hoc.

### How `google-services.json` is managed

1. The Android app is registered in Firebase with application id `com.danger.haztrack`.
2. The console (or a project owner) downloads `google-services.json`.
3. That file is **never committed**. It is listed in the root `.gitignore` as `google-services.json`.
4. Each developer copies the team file to `app/google-services.json` locally.
5. The `com.google.gms.google-services` plugin (applied in `app/build.gradle.kts`) reads it at build time.

Treat it as a secret even though some values inside are also considered “client-visible” once the APK is built. Sharing a private copy still exposes OAuth client IDs and makes it easy to point a rogue build at the real project. **Ask the main developer for the file; do not check it into git or a gist.**

If Google Sign-In fails with an OAuth / SHA mismatch after you added the file, your debug SHA-1 is probably missing from the Firebase Android app. Send `./gradlew signingReport` output to the main developer rather than rotating client IDs yourself.

### Firebase products that must already be enabled

These are configured on the shared project (again: main developer / project owner), not by cloning the repo:

- Authentication → **Email/Password**
- Authentication → **Google**
- **Cloud Firestore** (Native mode)
- Authentication email template custom action URL for password reset (Hosting `resetPassword` handler)
- Hosting site serving `assetlinks.json` for App Links

---

## Profile photo backend (optional)

Changing a profile picture requires the Node upload service described in [`docs/backend-image-upload-spec.md`](docs/backend-image-upload-spec.md). Auth, profile fields, and the rest of the UI work without it.

The base URL is **not hardcoded**. Gradle reads `BACKEND_BASE_URL` from `local.properties` and exposes it as `BuildConfig.BACKEND_BASE_URL`. If the property is omitted, the default is `http://10.0.2.2:4000/api/v1/` (Android emulator → host loopback).

Example `local.properties` entry (in addition to `sdk.dir`):

```properties
BACKEND_BASE_URL=http://10.0.2.2:4000/api/v1/
```

Debug builds allow cleartext HTTP only to `10.0.2.2`, `localhost`, and `127.0.0.1`. Release builds do not. A physical device typically needs `adb reverse` plus `http://127.0.0.1:...` or a TLS tunnel. **Ask the main developer** how the upload service is run and which URL to use; do not put Cloudinary API secrets in the Android app.

---

## Tech stack (short)

Kotlin, Jetpack Compose, Material 3, MVVM, Hilt, Navigation Compose, Firebase Auth + Firestore, Retrofit/OkHttp/Moshi for the upload API, Coil for images. Versions live in `gradle/libs.versions.toml`.

Min SDK 24, target SDK 36.

---

## Further documentation

| Doc | Contents |
|---|---|
| [`docs/docs.md`](docs/docs.md) | Architecture, MVVM, screens, auth/profile flows, Hilt, setup notes |
| [`docs/deeplinks-firebase-hosting.md`](docs/deeplinks-firebase-hosting.md) | Password-reset App Links and Hosting |
| [`docs/backend-image-upload-spec.md`](docs/backend-image-upload-spec.md) | Upload API the Android client expects |
| [`docs/changelog.md`](docs/changelog.md) | Feature and fix history |
