# Password-Reset Deep Links (Supabase)

This guide explains how Haztrack handles forgot-password emails after Firebase Hosting
was removed. It is written for developers who are new to custom URI schemes and
Supabase recovery redirects.

## 1. What the user experiences

1. The user opens Forgot Password, enters an email, and taps Send reset link.
2. Supabase sends a password-reset email.
3. The user taps the link on a device with the app installed.
4. Android opens Haztrack because the link uses the app's custom scheme.
5. `MainActivity` waits for Supabase Auth to finish initializing, then imports
   the recovery session from the full URL.
6. Navigation opens `ResetPasswordScreen` with the recovered email.
7. The user sets a new password. The app calls Supabase `updateUser` on the
   recovery session.
8. The user is sent back to Login.

There is no in-repo browser fallback page and no Firebase Hosting site. If the
app is not installed, the custom scheme does not open Haztrack; that case is
handled (if at all) by Supabase's own redirect configuration, not by this
repository.

## 2. The recovery URL

Forgot-password requests set the redirect to:

```
com.danger.haztrack://reset-password
```

That value is passed to `auth.resetPasswordForEmail(email, redirectUrl)` in
`AuthRemoteDataSource`. `SupabaseModule` configures the Auth plugin with the
same scheme and host so the SDK can parse the incoming URL:

- `scheme = "com.danger.haztrack"`
- `host = "reset-password"`
- `flowType = FlowType.IMPLICIT`

Google Sign-In does **not** use this redirect. It is a native ID-token exchange
through Credential Manager.

## 3. Android intent filter

`AndroidManifest.xml` declares a `VIEW` / `BROWSABLE` filter on `MainActivity`
(`launchMode="singleTask"`):

```xml
<data
    android:scheme="com.danger.haztrack"
    android:host="reset-password"/>
```

This is a **custom URI scheme**, not a verified Android App Link. There is no
`assetlinks.json`, no Digital Asset Links check, and no HTTPS host in this
project. Cold start and already-running cases both go through `handleIntent`
(`onCreate` and `onNewIntent`).

## 4. How the app consumes the link

```
Email link
        │
        ▼
MainActivity.handleIntent
  scheme/host must match
  awaitSessionReady()
  establishSessionFromUrl(url)
        │
        ▼
recoveryEmailFlow ← AuthUser.email
        │
        ▼
HaztrackNavHost → ResetPasswordScreen
        │
        ▼
updatePassword(newPassword)
```

`EstablishSessionFromUrlUseCase` / `AuthRemoteDataSource` import the session
tokens from the URL. `UpdatePasswordUseCase` then updates the authenticated
user. There is no Firebase `oobCode` verification step.

If session import fails, the failure is logged with Timber; the user can
request a new email from Forgot Password.

## 5. What you must configure (outside this repo)

These live on the shared Supabase project (ask the main developer; do not
create a separate project):

- Email templates / Auth URL configuration so recovery links use
  `com.danger.haztrack://reset-password`.
- Email/password auth enabled.
- Google provider enabled if you need Google Sign-In.

## 6. Testing

1. Build and install a debug APK.
2. Request a reset from Forgot Password using an account you control.
3. Open the email on the same device/emulator.
4. Confirm the app opens `ResetPasswordScreen` (not Login with a dead link).
5. Set a new password and sign in with it.

If the mail client does not treat the custom scheme as tappable, paste the URL
into an `adb` command:

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "com.danger.haztrack://reset-password"
```

A real recovery URL from the email includes session fragments/query parameters
from Supabase; the empty host-only URL is only useful to confirm the intent
filter.

## 7. Security notes

- Recovery links are short-lived. Request a new email if import fails.
- Never log the full recovery URL in release builds if it contains tokens.
  Debug Timber lines exist for diagnosing link handling; do not paste those
  logs into public issues.
- Keep Supabase Auth calls in the data layer (`AuthRemoteDataSource`).
- Do not commit `SUPABASE_URL` / `SUPABASE_ANON_KEY` from `local.properties`.

## 8. Related files

- `app/src/main/AndroidManifest.xml` — custom-scheme intent filter.
- `app/src/main/java/com/danger/haztrack/MainActivity.kt` — import session from URL.
- `app/src/main/java/com/danger/haztrack/di/SupabaseModule.kt` — scheme, host, Implicit flow.
- `data/remote/api/AuthRemoteDataSource.kt` — `resetPasswordForEmail` redirect URL.
- `presentation/auth/forgotpassword/` and `presentation/auth/resetpassword/`.
- [`docs.md` §8.3](docs.md#83-password-reset)
