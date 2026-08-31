# Firebase Hosting and Password-Reset Deep Links

This guide explains how Haztrack turns a password-reset email into an
in-app password-reset screen. It is written for developers who are new to
Android deep links, Firebase Hosting, and the MVVM architecture used by this
project.

## 1. What This Feature Does

The password-reset feature has two parts:

1. The user enters an email address in `ForgotPasswordScreen`.
2. Firebase sends a password-reset email.
3. The user taps the link in the email.
4. Android recognizes the HTTPS link as belonging to Haztrack.
5. Android opens `MainActivity` and delivers the URL as an `Intent`.
6. Haztrack extracts Firebase's one-time reset code (`oobCode`).
7. Navigation opens `ResetPasswordScreen`.
8. Firebase verifies the code and returns the email address it belongs to.
9. The user enters a new password.
10. Firebase confirms the reset, and Haztrack returns the user to sign-in.

The website is not responsible for changing the password. Firebase Hosting
provides the HTTPS domain and the Android App Links association file. The
Android app performs the actual code verification and password change through
the Firebase Authentication SDK.

## 2. Important Terms

### Deep link

A deep link is a URL that opens a particular place in an app instead of only
opening the app's home screen. A URL can be a custom scheme such as
`haztrack://reset`, or an HTTPS URL such as
`https://haztrack-62a3c.firebaseapp.com/__/auth/links`.

### Android App Link

An Android App Link is a verified HTTPS deep link. It is preferable to a
custom scheme for this feature because:

- It uses a normal HTTPS URL.
- Android can verify that the domain authorizes the app.
- The link can still open in a browser if the app is not installed.
- Another app cannot claim the verified domain in the same way as an
  unverified custom scheme.

The word "verified" is important. Declaring an intent filter in
`AndroidManifest.xml` tells Android that the app wants to handle a URL.
`assetlinks.json` on the website proves that the website agrees to associate
that URL with this package and signing certificate.

### Intent

An `Intent` is an Android message describing an action. When the user taps the
password-reset URL, Android starts or reuses `MainActivity` and places the URL
in `intent.data`.

Haztrack handles both lifecycle cases:

- `onCreate()` handles a link that launches a new activity.
- `onNewIntent()` handles a link delivered to an already-running activity.

### Firebase action code

Firebase places a short-lived, single-use value called an `oobCode` in the
password-reset URL. "OOB" means "out of band": the code was delivered outside
the app, through the email.

The code is not a password. It is a temporary capability that Firebase uses
to identify and authorize this reset operation. It must not be logged,
shared, committed, or included in screenshots.

## 3. The End-to-End Architecture

The feature follows the project's MVVM and layered architecture:

```text
ForgotPasswordScreen
        |
        v
ForgotPasswordViewModel
        |
        v
SendPasswordResetEmailUseCase
        |
        v
AuthRepository
        |
        v
AuthRepositoryImpl
        |
        v
AuthRemoteDataSource
        |
        v
FirebaseAuth.sendPasswordResetEmail(...)
```

The deep-link half begins outside the normal screen flow:

```text
Password-reset email
        |
        v
HTTPS Firebase Hosting link
        |
        v
Android App Links verification
        |
        v
MainActivity Intent handling
        |
        v
StateFlow<String?> containing oobCode
        |
        v
HaztrackNavHost
        |
        v
ResetPasswordScreen
        |
        v
ResetPasswordViewModel
        |
        +--> VerifyPasswordResetCodeUseCase
        |
        +--> ConfirmPasswordResetUseCase
```

This separation gives each layer one job:

- `MainActivity` receives Android intents and extracts technical URL data.
- `HaztrackNavHost` decides which Compose destination to display.
- `ResetPasswordScreen` renders fields, buttons, progress, and messages.
- `ResetPasswordViewModel` owns screen state and launches coroutines.
- Use cases represent password-reset actions in the domain layer.
- The repository hides the Firebase implementation from the domain layer.
- `AuthRemoteDataSource` makes the actual Firebase SDK calls.

## 4. How Firebase Creates the Link

When the user requests a reset link, `AuthRemoteDataSource` creates
`ActionCodeSettings` and passes it to Firebase:

```kotlin
val actionCodeSettings = actionCodeSettings {
    url = "https://haztrack-62a3c.firebaseapp.com/resetPassword"
    handleCodeInApp = true
    setAndroidPackageName(
        "com.danger.haztrack",
        true,
        null,
    )
}
firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings).await()
```

The important settings are:

- `url` is the continue URL associated with the operation.
- `handleCodeInApp = true` asks Firebase to create an in-app action link.
- `setAndroidPackageName(...)` tells Firebase which Android package should
  receive the link.
- `sendPasswordResetEmail(...)` sends the generated link by email.

The final URL is generated by Firebase. Its exact parameter order may vary,
but it contains information similar to this:

```text
https://haztrack-62a3c.firebaseapp.com/__/auth/links
    ?apiKey=...
    &mode=resetPassword
    &oobCode=...
    &link=https%3A%2F%2Fhaztrack-62a3c.firebaseapp.com%2FresetPassword%3F...
```

The URL may contain a nested URL in the `link` query parameter. That is why
the app checks for `link` first and parses it as another `Uri`. The app then
reads `mode` and `oobCode` from the action URL.

The `...` values above are placeholders. Never hardcode or document a real
reset code.

## 5. How Android Matches the Link

Haztrack declares an HTTPS intent filter on `MainActivity`:

```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data
        android:scheme="https"
        android:host="haztrack-62a3c.firebaseapp.com"
        android:pathPrefix="/__/auth/links"/>
</intent-filter>
```

Each part has a purpose:

- `VIEW` means the activity can display a resource represented by a URL.
- `DEFAULT` allows normal implicit intent resolution.
- `BROWSABLE` allows links clicked from a browser, email, or another app.
- `https` restricts the scheme to secure web URLs.
- `host` restricts the link to the Haztrack Firebase Hosting domain.
- `pathPrefix` restricts matching to Firebase's action-link path.
- `autoVerify` asks Android to verify the domain association automatically.

The manifest filter does not itself grant ownership of the domain. Android
also requests:

```text
https://haztrack-62a3c.firebaseapp.com/.well-known/assetlinks.json
```

That file must be publicly reachable and must authorize the exact Android
package name and signing certificate used by the installed APK.

## 6. How `assetlinks.json` Works

The current file is:

```json
[
  {
    "relation": [
      "delegate_permission/common.handle_all_urls"
    ],
    "target": {
      "namespace": "android_app",
      "package_name": "com.danger.haztrack",
      "sha256_cert_fingerprints": [
        "DEBUG_CERTIFICATE_FINGERPRINT"
      ]
    }
  }
]
```

The real repository file contains the current debug certificate fingerprint.
The placeholder above is intentional so a certificate value is not copied
into documentation.

These values must match:

- `package_name`: the application's final Android application ID.
- `sha256_cert_fingerprints`: the SHA-256 fingerprint of the certificate
  signing the installed APK.
- `relation`: `delegate_permission/common.handle_all_urls` allows the app to
  handle URLs for the declared domain.

Debug and release builds normally use different signing certificates. The
debug fingerprint is therefore not enough for production. Before release,
add the release certificate fingerprint or the Play App Signing fingerprint
to `public/.well-known/assetlinks.json`, then redeploy Hosting.

Do not confuse SHA-1 and SHA-256. Firebase Google Sign-In setup commonly asks
for SHA-1, while Digital Asset Links requires SHA-256.

## 7. What Firebase Hosting Does

The repository's `firebase.json` configures Firebase Hosting:

```json
{
  "hosting": {
    "public": "public",
    "headers": [
      {
        "source": "/.well-known/assetlinks.json",
        "headers": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ]
      }
    ]
  }
}
```

The important parts are:

- `public` tells Firebase which directory to upload.
- `public/.well-known/assetlinks.json` is the Android domain-association
  file.
- The header ensures the association file is served as JSON.
- `public/index.html` is the default Hosting page.
- `public/404.html` is the default Hosting error page.

The `public` HTML pages are not the password-reset form. The form is rendered
by the Android app. Hosting mainly provides the domain, Firebase's reserved
`/__/auth/links` endpoint, and the association file required by Android.

The selected Firebase project is stored in `.firebaserc`. In this project it
is `haztrack-62a3c`.

## 8. How the App Receives and Routes the Code

`MainActivity` processes the incoming URL in `handleIntent()`:

1. Read `intent.data`.
2. Read the nested `link` parameter if Firebase provided one.
3. Parse the nested value as a `Uri`.
4. Check that `mode` equals `resetPassword`.
5. Read `oobCode`.
6. Publish the code through `oobCodeFlow`.

`oobCodeFlow` is a `MutableStateFlow<String?>`. The Activity owns this
short-lived event source because it is the Android entry point, while the
Compose navigation layer observes it.

`HaztrackNavHost` collects the flow and navigates to:

```text
reset_password/{oobCode}
```

The route argument is read by `ResetPasswordViewModel` through
`SavedStateHandle`. The ViewModel then:

1. Calls `VerifyPasswordResetCodeUseCase`.
2. Marks the code valid and shows the email on success.
3. Enables the reset button only when the code is valid and the new password
   is at least six characters.
4. Calls `ConfirmPasswordResetUseCase` with the same code and new password.
5. Reports success so navigation can return to Login.

The navigation back stack is cleared after a successful reset. This prevents
the user from pressing Back and returning to a stale reset screen.

## 9. Local Development and Deployment

### Prerequisites

You need:

- A Firebase project configured for the Android application.
- Firebase Authentication with the required sign-in providers enabled.
- The Firebase CLI installed and authenticated.
- `app/google-services.json` for the Android build.
- The correct debug or release SHA-256 certificate fingerprint.

### Deploy the Hosting files

From the repository root:

```bash
firebase deploy --only hosting
```

After deployment, check the association file:

```bash
curl -i https://haztrack-62a3c.firebaseapp.com/.well-known/assetlinks.json
```

Confirm that:

- The response is successful.
- The response `Content-Type` is `application/json`.
- The JSON is valid.
- The package name and fingerprint match the APK being tested.

### Build and install the debug app

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The project documentation uses `assembleDebug` as the standard build check.
Run the project's configured lint and static-analysis tasks separately.

### Test with a real email

1. Deploy Hosting.
2. Install the debug APK.
3. Request a password-reset email from the app.
4. Tap the link on the Android device.
5. Confirm that the app opens instead of only opening a browser.
6. Confirm that the reset screen displays the verified account email.
7. Enter a valid new password and submit it.
8. Sign in with the new password.

Use a test account during development. Treat every reset link as a secret.

### Test URL dispatch with ADB

If you have a complete test action URL, you can ask Android to dispatch it:

```bash
adb shell am start \
  -a android.intent.action.VIEW \
  -c android.intent.category.BROWSABLE \
  -d "https://haztrack-62a3c.firebaseapp.com/__/auth/links?..."
```

Do not paste a real reset code into shared terminals, issue trackers, or
commits. A reset code is single-use and should be discarded after testing.

To inspect App Links status on a supported Android device:

```bash
adb shell pm get-app-links --user 0 com.danger.haztrack
```

## 10. Troubleshooting

### The link opens only in the browser

Check all of the following:

- The APK is installed on the device.
- The URL uses `https`.
- The host exactly matches `haztrack-62a3c.firebaseapp.com`.
- The path begins with `/__/auth/links`.
- `assetlinks.json` is reachable without authentication.
- The package name matches `com.danger.haztrack`.
- The fingerprint matches the certificate that signed the installed APK.
- Hosting was redeployed after changing `assetlinks.json`.

Android may cache verification results. Reinstalling the app or re-running
App Links verification can help during development.

### The app opens, but the reset screen does not appear

Check that:

- The Firebase URL contains `mode=resetPassword`.
- The URL contains an `oobCode`.
- If there is a `link` parameter, its decoded value is a valid URL.
- The intent filter path matches the outer Firebase action-link URL.
- The app handles both cold launch and an already-running activity.

### The reset screen says the link is invalid or expired

Firebase reset codes are temporary and single-use. Request a new email and
use the newest link. Do not test the same link repeatedly after a successful
reset.

### The association file returns HTML

This usually means the path is missing or Hosting is serving the 404 page.
Check the exact `/.well-known/assetlinks.json` path and confirm the
`firebase.json` header configuration. The response must be JSON, not the
default Hosting HTML page.

### Debug works but release does not

The release APK is probably signed with a different certificate. Add the
release or Play App Signing SHA-256 fingerprint to `assetlinks.json` and
deploy again. Also verify that the release application ID and manifest host
are unchanged.

## 11. Development Rules and Safety

- Never log the full incoming URL or `oobCode`.
- Never log passwords or include them in analytics events.
- Never commit reset links, Firebase credentials, or signing key material.
- Keep Firebase SDK calls in the data layer.
- Keep navigation and screen state decisions in ViewModels or navigation
  components, not in the data source.
- Use `StateFlow` for observable state and lifecycle-aware collection in
  Compose.
- Keep `assetlinks.json` synchronized with every certificate used to
  distribute the app.
- Treat the debug association as a development configuration, not a
  production release configuration.

The reset code authorizes a sensitive account operation. It is acceptable to
pass it from the Android intent into the navigation destination because the
feature needs it, but minimize its lifetime and never expose it through
logs, crash messages, screenshots, or copied documentation.

## 12. Files Involved

The main files for this feature are:

- `app/src/main/AndroidManifest.xml` — declares the verified HTTPS intent
  filter.
- `app/src/main/java/com/danger/haztrack/MainActivity.kt` — receives and
  parses incoming URLs.
- `app/src/main/java/com/danger/haztrack/presentation/navigation/HaztrackDestination.kt`
  — defines the reset route.
- `app/src/main/java/com/danger/haztrack/presentation/navigation/HaztrackNavHost.kt`
  — observes the code and navigates to the reset screen.
- `app/src/main/java/com/danger/haztrack/presentation/auth/resetpassword/`
  — renders the form and manages reset state.
- `app/src/main/java/com/danger/haztrack/domain/usecase/auth/`
  — contains the verify and confirm reset use cases.
- `app/src/main/java/com/danger/haztrack/data/remote/api/AuthRemoteDataSource.kt`
  — configures Firebase action links and calls Firebase Auth.
- `firebase.json` — configures Firebase Hosting and response headers.
- `public/.well-known/assetlinks.json` — authorizes Android App Links.
- `.firebaserc` — selects the Firebase project for CLI commands.

When changing the Firebase project or domain, update every place that depends
on it: `ActionCodeSettings`, the manifest host, the Hosting project, and the
deployed `assetlinks.json` file.
