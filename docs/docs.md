# Haztrack — Developer Documentation

> **Current state:** Authentication scaffold only. The full hazard-tracking product domain has not been implemented yet. All placeholder packages (`data/local`, `data/remote/dto`, `data/service`, `util`, `presentation/common`) are empty and reserved for future features.

---

## Table of Contents

1. [What Is This App?](#1-what-is-this-app)
2. [Tech Stack](#2-tech-stack)
3. [Project File Structure](#3-project-file-structure)
4. [Understanding MVVM (For Beginners)](#4-understanding-mvvm-for-beginners)
5. [Architecture Layers Explained](#5-architecture-layers-explained)
   - [Data Layer](#51-data-layer)
   - [Domain Layer](#52-domain-layer)
   - [Presentation Layer](#53-presentation-layer)
6. [Dependency Injection with Hilt](#6-dependency-injection-with-hilt)
7. [Navigation](#7-navigation)
8. [Authentication Features](#8-authentication-features)
   - [Firebase Email Authentication](#81-firebase-email-authentication)
   - [Google Sign-In](#82-google-sign-in)
   - [Password Reset](#83-password-reset)
   - [Session Persistence](#84-session-persistence)
   - [Sign Out](#85-sign-out)
9. [UI Components](#9-ui-components)
10. [Error Handling](#10-error-handling)
11. [Build, Lint, and Code Quality](#11-build-lint-and-code-quality)
12. [Setting Up Locally](#12-setting-up-locally)
13. [Firebase Hosting and Password-Reset Deep Links](deeplinks-firebase-hosting.md)

---

## 1. What Is This App?

**Haztrack** (`com.danger.haztrack`) is an Android application intended for hazardous-material tracking. The current version implements a complete, production-quality authentication system as the foundation for the rest of the product.

**Implemented screens:**

| Screen | Route | Description |
|---|---|---|
| Login | `login` | Email/password + Google Sign-In |
| Register | `register` | Create account with email/password |
| Forgot Password | `forgot_password` | Send a password-reset email |
| Home | `home` | Post-sign-in landing screen, shows the signed-in user |

---

## 2. Tech Stack

| Category | Library / Tool | Version |
|---|---|---|
| Language | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | BOM 2024.09.00 |
| Architecture | MVVM | — |
| Navigation | Navigation Compose | 2.9.8 |
| Dependency Injection | Hilt | 2.60.1 |
| Authentication | Firebase Auth | BOM 34.18.0 |
| Google Sign-In | AndroidX Credentials + Google Identity | 1.6.0 / 1.2.0 |
| Coroutines | Kotlin Coroutines + Play Services adapter | 1.11.0 |
| Logging | Timber | 5.0.1 |
| Static Analysis | Detekt | 2.0.0-alpha.6 |
| Build | Gradle (Kotlin DSL) | 9.3.1 wrapper |
| Min SDK | Android 7.0 (Nougat) | API 24 |
| Target SDK | Android 16 | API 36 |

Dependencies are managed centrally in `gradle/libs.versions.toml` using Gradle's **Version Catalog** — one source of truth for all versions. You never hardcode version numbers in individual `build.gradle.kts` files.

---

## 3. Project File Structure

```
haztrack/
├── build.gradle.kts                    # Root Gradle: applies plugins, does NOT declare dependencies
├── settings.gradle.kts                 # Registers the :app module; configures repositories
├── gradle/
│   ├── libs.versions.toml              # Version catalog: all dependency/plugin versions in one file
│   └── wrapper/gradle-wrapper.properties
├── config/detekt/detekt.yml            # Detekt static analysis rules
├── .githooks/
│   ├── pre-commit                      # Runs lint/detekt before every commit
│   └── commit-msg                      # Enforces commit message format
├── firebase.json                       # Firebase Hosting configuration and headers
├── public/
│   ├── index.html                       # Firebase Hosting entry page
│   ├── 404.html                         # Firebase Hosting fallback page
│   ├── resetPassword/
│   │   └── index.html                   # Browser password-reset page
│   └── .well-known/
│       └── assetlinks.json              # Android App Links association
└── app/
    ├── build.gradle.kts                # App module: plugins, android{}, dependencies
    ├── google-services.json            # Firebase config — GITIGNORED, never commit this
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml         # App entry point, permissions, activity declarations
        ├── res/
        │   ├── values/strings.xml      # ALL user-facing text lives here (no hardcoded strings)
        │   ├── values/themes.xml       # App theme (no action bar)
        │   ├── drawable/
        │   │   ├── ic_app_logo.png     # Login screen branding image
        │   │   └── ic_google.xml       # Google logo vector
        │   └── mipmap-*/               # Launcher icons (multiple densities)
        └── java/com/danger/haztrack/
            ├── HaztrackApplication.kt  # @HiltAndroidApp — Hilt entry point
            ├── MainActivity.kt         # @AndroidEntryPoint — single Activity, hosts Compose
            │
            ├── di/                     # Dependency Injection (Hilt modules)
            │   ├── FirebaseModule.kt   # Provides FirebaseAuth singleton
            │   └── RepositoryModule.kt # Binds AuthRepositoryImpl → AuthRepository interface
            │
            ├── data/                   # DATA LAYER: knows about Firebase, databases, APIs
            │   ├── remote/
            │   │   └── api/
            │   │       └── AuthRemoteDataSource.kt  # Calls Firebase Auth directly
            │   ├── repository/
            │   │   └── auth/
            │   │       └── AuthRepositoryImpl.kt    # Implements domain AuthRepository
            │   ├── local/              # (empty — future: Room database)
            │   ├── remote/dto/         # (empty — future: API response models)
            │   └── service/            # (empty — future: background services)
            │
            ├── domain/                 # DOMAIN LAYER: pure Kotlin, zero Android dependencies
            │   ├── model/
            │   │   └── AuthUser.kt     # App's user model (NOT FirebaseUser)
            │   ├── repository/auth/
            │   │   └── AuthRepository.kt  # Interface — the contract the data layer must fulfil
            │   └── usecase/auth/
            │       ├── AuthInputValidation.kt       # Email regex, password length rules
            │       ├── AuthUseCases.kt              # Facade: bundles all auth use cases
            │       ├── GetCurrentUserUseCase.kt
            │       ├── SignInWithEmailUseCase.kt
            │       ├── SignUpWithEmailUseCase.kt
            │       ├── SignInWithGoogleUseCase.kt
            │       ├── SendPasswordResetEmailUseCase.kt
            │       ├── VerifyPasswordResetCodeUseCase.kt
            │       ├── ConfirmPasswordResetUseCase.kt
            │       └── SignOutUseCase.kt
            │
            ├── presentation/           # PRESENTATION LAYER: Compose UI + ViewModels
            │   ├── navigation/
            │   │   ├── HaztrackDestination.kt  # Type-safe route strings (sealed class)
            │   │   ├── HaztrackNavHost.kt       # NavHost composable — wires all screens
            │   │   └── SessionViewModel.kt      # Decides start destination on cold launch
            │   │
            │   ├── auth/
            │   │   ├── common/
            │   │   │   ├── AuthErrorMapper.kt   # Maps exceptions → string resource IDs
            │   │   │   └── GoogleAuthClient.kt  # Wraps Credential Manager (needs Context)
            │   │   ├── login/
            │   │   │   ├── LoginScreen.kt
            │   │   │   ├── LoginViewModel.kt
            │   │   │   ├── LoginUiState.kt
            │   │   │   └── LoginEvent.kt
            │   │   ├── register/
            │   │   │   ├── RegisterScreen.kt
            │   │   │   ├── RegisterViewModel.kt
            │   │   │   ├── RegisterUiState.kt
            │   │   │   └── RegisterEvent.kt
            │   │   ├── forgotpassword/
            │   │   │   ├── ForgotPasswordScreen.kt
            │   │   │   ├── ForgotPasswordViewModel.kt
            │   │   │   └── ForgotPasswordUiState.kt
            │   │   └── resetpassword/
            │   │       ├── ResetPasswordScreen.kt
            │   │       ├── ResetPasswordViewModel.kt
            │   │       └── ResetPasswordUiState.kt
            │   │
            │   ├── home/
            │   │   ├── HomeScreen.kt
            │   │   ├── HomeViewModel.kt
            │   │   ├── HomeUiState.kt
            │   │   └── HomeEvent.kt
            │   │
            │   ├── components/         # Reusable Compose components shared across screens
            │   │   ├── AuthDivider.kt           # "OR" divider line
            │   │   ├── AuthTopBar.kt            # Back-button top bar
            │   │   ├── GoogleSignInButton.kt    # Branded Google button
            │   │   ├── HaztrackPasswordField.kt # Password field with show/hide toggle
            │   │   ├── HaztrackPrimaryButton.kt # Primary CTA button with loading state
            │   │   └── HaztrackTextField.kt     # Styled text input field
            │   │
            │   ├── theme/
            │   │   ├── Color.kt        # Palette and Material 3 ColorSchemes (light + dark)
            │   │   ├── Theme.kt        # HaztrackTheme composable
            │   │   └── Type.kt         # Typography scale
            │   │
            │   └── common/             # (empty — future: shared presentation utilities)
            │
            └── util/                   # (empty — future: extension functions, helpers)
```

---

## 4. Understanding MVVM (For Beginners)

MVVM stands for **Model – View – ViewModel**. It is a way to organise code so that each piece has exactly one responsibility and nothing more. This makes the code easier to test, easier to change, and easier to understand.

### The Three Parts

**Model** — the data and business rules. In this project this is the **Domain** and **Data** layers combined: `AuthUser`, `AuthRepository`, use cases, and Firebase calls.

**View** — what the user sees. In this project the View is every Compose `@Composable` function (the `*Screen.kt` files). The composable's only job is to **draw** the UI and **report** user events (button taps, text input) upward. It never makes decisions.

**ViewModel** — the brain between Model and View. The ViewModel:
- Holds the current state of the screen as an **immutable** data class (`*UiState`).
- Receives user events from the View (e.g., `onSignInClick()`).
- Calls use cases to execute business logic.
- Updates the state based on the result.
- Never holds a reference to the composable or Android `Context`.

### One-Way Data Flow

Data only travels in one direction. The diagram below shows the full flow for a button tap:

```
User taps "Sign In"
        │
        ▼
LoginScreen (View)
  calls viewModel.onSignInClick()
        │
        ▼
LoginViewModel (ViewModel)
  launches a coroutine
  calls authUseCases.signInWithEmail(email, password)
        │
        ▼
SignInWithEmailUseCase (Domain)
  validates input (AuthInputValidation)
  calls authRepository.signInWithEmail(...)
        │
        ▼
AuthRepositoryImpl (Data)
  calls authRemoteDataSource.signInWithEmail(...)
        │
        ▼
AuthRemoteDataSource (Data)
  calls Firebase Auth SDK
  returns FirebaseUser
        │
        ▲
AuthRepositoryImpl
  converts FirebaseUser → AuthUser (domain model)
  returns AuthUser
        │
        ▲
SignInWithEmailUseCase
  returns AuthUser to ViewModel
        │
        ▲
LoginViewModel
  updates _uiState (isLoading = false)
  sends NavigateToHome event via Channel
        │
        ▲
LoginScreen
  collectAsStateWithLifecycle() sees new uiState → recomposes
  LaunchedEffect sees the NavigateToHome event → calls onSignedIn()
        │
        ▲
HaztrackNavHost
  navigates to the Home screen
```

### Why Is This Useful?

- **Separation of concerns**: Firebase could be swapped for another provider by only changing `AuthRemoteDataSource` — nothing else needs to change.
- **Testability**: `LoginViewModel` can be unit-tested by providing a fake `AuthUseCases` — no emulator, no Firebase, no Android needed.
- **Predictability**: You always know where state lives (ViewModel), where UI decisions are made (ViewModel), and where Android-specific concerns go (Screen / `GoogleAuthClient`).

---

## 5. Architecture Layers Explained

### 5.1 Data Layer

**Location:** `app/src/main/java/com/danger/haztrack/data/`

The data layer is the only place in the app that knows about external services (Firebase, future REST APIs, Room database). Nothing outside this layer imports Firebase classes.

#### `AuthRemoteDataSource`

```
data/remote/api/AuthRemoteDataSource.kt
```

A `@Singleton` class injected with `FirebaseAuth`. It performs raw Firebase operations and returns `FirebaseUser` objects. All Firebase calls are `suspend` functions using `.await()` (the coroutines-play-services adapter that converts Firebase `Task<T>` into a coroutine you can `await`).

```kotlin
suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
    return firebaseAuth.signInWithEmailAndPassword(email, password).await().user
        ?: error("Firebase did not return a user after email sign-in")
}
```

If Firebase returns `null` for the user (which should not happen under normal conditions), the `?:` Elvis operator throws an `IllegalStateException` immediately rather than letting a `NullPointerException` crash somewhere deeper.

#### `AuthRepositoryImpl`

```
data/repository/auth/AuthRepositoryImpl.kt
```

Implements the `AuthRepository` **interface** defined in the domain layer. Its job is:
1. Delegate every call to `AuthRemoteDataSource`.
2. Convert `FirebaseUser` (a Firebase type) into `AuthUser` (the app's own domain model) so the domain layer stays independent of Firebase.

```kotlin
private fun FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        id = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified,
    )
}
```

This conversion is a private **extension function** on `FirebaseUser`. Extension functions in Kotlin let you add methods to classes you don't own — here it reads as "convert this `FirebaseUser` into an `AuthUser`".

---

### 5.2 Domain Layer

**Location:** `app/src/main/java/com/danger/haztrack/domain/`

The domain layer contains **pure Kotlin** — no Android imports, no Firebase imports, no Compose imports. It defines what the app can do (use cases) and the shape of app data (models), without caring about how it is done.

#### `AuthUser` — the Domain Model

```
domain/model/AuthUser.kt
```

```kotlin
data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
)
```

A `data class` in Kotlin auto-generates `equals()`, `hashCode()`, and `copy()`. This is the canonical user object used everywhere above the data layer. The presentation layer never imports `FirebaseUser`.

#### `AuthRepository` — the Interface Contract

```
domain/repository/auth/AuthRepository.kt
```

```kotlin
interface AuthRepository {
    fun getCurrentUser(): AuthUser?
    suspend fun signInWithEmail(email: String, password: String): AuthUser
    suspend fun signUpWithEmail(email: String, password: String): AuthUser
    suspend fun signInWithGoogle(idToken: String): AuthUser
    suspend fun sendPasswordResetEmail(email: String)
    fun signOut()
}
```

An `interface` is a contract. It says "whatever implements me must provide these functions". Hilt wires `AuthRepositoryImpl` as the concrete implementation (in `RepositoryModule`). The domain layer — and everything above it — only ever sees this interface, not the implementation. This is the key to swappability and testability.

#### Use Cases

Each use case wraps a single action and is its own class. They all follow the same pattern: inject `AuthRepository`, expose a single `invoke()` operator so callers can call them like a function.

**`AuthInputValidation`** — validates user input before it reaches the repository:
- Email must match a standard regex pattern.
- Password must be at least 6 characters.
- Google ID token must not be blank.

`require(condition) { "message" }` is a Kotlin standard function that throws `IllegalArgumentException` if the condition is false. `AuthErrorMapper` catches this and maps it to `R.string.auth_error_invalid_input`.

**`AuthUseCases`** — a `data class` that groups all use cases into one injectable object:

```kotlin
data class AuthUseCases @Inject constructor(
    val getCurrentUser: GetCurrentUserUseCase,
    val signInWithEmail: SignInWithEmailUseCase,
    val signUpWithEmail: SignUpWithEmailUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val sendPasswordResetEmail: SendPasswordResetEmailUseCase,
    val signOut: SignOutUseCase,
)
```

ViewModels inject `AuthUseCases` instead of six separate use cases. This keeps ViewModel constructor parameters tidy and groups the auth API in one place.

---

### 5.3 Presentation Layer

**Location:** `app/src/main/java/com/danger/haztrack/presentation/`

This layer contains everything the user sees and the logic that drives it.

#### Screen Pattern: UiState + Events + ViewModel

Every feature screen follows the same three-file contract (plus the Screen composable):

| File | Purpose |
|---|---|
| `*UiState.kt` | Immutable data class — a snapshot of everything the screen needs to render |
| `*Event.kt` | Sealed interface — one-shot navigation events (fire-and-forget) |
| `*ViewModel.kt` | Holds `StateFlow<*UiState>` + `Flow<*Event>`; handles user actions |
| `*Screen.kt` | Composable that observes state and wires up the ViewModel |

#### Why Two Different Mechanisms? `StateFlow` vs `Channel`

**`StateFlow<UiState>`** — used for screen state (loading spinners, text field values, error messages). It replays the latest value so a Composable always has a state to render, even if it starts collecting late.

**`Channel<Event>` (exposed as `Flow<Event>`)** — used for navigation and other one-shot side effects. A `Channel` does not replay: if the user has already navigated to Home, you don't want the event replayed when rotating the device. Navigation should only happen once.

```kotlin
// ViewModel — state (observed continuously)
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

// ViewModel — events (one-shot)
private val _events = Channel<LoginEvent>(Channel.BUFFERED)
val events: Flow<LoginEvent> = _events.receiveAsFlow()
```

```kotlin
// Screen — collecting both
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            LoginEvent.NavigateToHome -> onSignedIn()
        }
    }
}
```

`collectAsStateWithLifecycle()` is lifecycle-aware: it stops collecting when the screen is not visible (e.g., app is in the background) and resumes when it returns to the foreground. This prevents wasted work and potential crashes.

`LaunchedEffect(Unit)` runs the event-collection coroutine once when the composable enters the Composition and cancels it automatically when the composable leaves.

#### `LoginUiState` — Derived State With a Computed Property

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleSignInLoading: Boolean = false,
    val errorMessageRes: Int? = null,
) {
    val isSignInEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading && !isGoogleSignInLoading
}
```

`isSignInEnabled` is a **computed property** — it is not stored separately but calculated from the other fields every time it is read. Because `LoginUiState` is a `data class`, Compose can compare old and new states efficiently and only recompose the parts of the UI that actually changed.

---

## 6. Dependency Injection with Hilt

Hilt is a DI (Dependency Injection) framework that automatically creates and provides objects to the classes that need them. Without DI, every class would be responsible for constructing its own dependencies — making code hard to test and tightly coupled.

### How Hilt Is Set Up

**Step 1 — Application**
```kotlin
@HiltAndroidApp
class HaztrackApplication : Application()
```
`@HiltAndroidApp` generates the Hilt component hierarchy. This is the starting point.

**Step 2 — Activity**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```
`@AndroidEntryPoint` makes Hilt aware of this Activity so it can inject into it (and into composables via `hiltViewModel()`).

**Step 3 — Hilt Modules**

Modules tell Hilt how to create objects it cannot create automatically (e.g., singletons from third-party SDKs).

`FirebaseModule` uses `@Provides` (for constructing a concrete object):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
```

`RepositoryModule` uses `@Binds` (for telling Hilt "when someone asks for `AuthRepository`, give them `AuthRepositoryImpl`"):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
```

`@InstallIn(SingletonComponent::class)` means these bindings live for the entire lifetime of the app — only one instance is ever created.

**Step 4 — Constructor Injection**

Classes annotated with `@Inject constructor(...)` have their dependencies provided by Hilt automatically:
```kotlin
@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Hilt looks this up from FirebaseModule
)
```

**Step 5 — ViewModels**
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel()
```

In the composable:
```kotlin
val viewModel: LoginViewModel = hiltViewModel()
```
`hiltViewModel()` is Navigation Compose-aware: it scopes the ViewModel to the current NavBackStackEntry, so it is automatically cleared when the user navigates away from that screen.

### The Full DI Chain

```
FirebaseAuth (from FirebaseModule)
    → injected into AuthRemoteDataSource
        → injected into AuthRepositoryImpl
            → bound as AuthRepository (from RepositoryModule)
                → injected into each UseCase
                    → injected into AuthUseCases
                        → injected into each ViewModel
```

---

## 7. Navigation

Navigation in this app uses **Navigation Compose** with no XML navigation graph. Everything is defined in Kotlin.

### `HaztrackDestination` — Type-Safe Routes

```kotlin
sealed class HaztrackDestination(val route: String) {
    data object Login        : HaztrackDestination("login")
    data object Register     : HaztrackDestination("register")
    data object ForgotPassword : HaztrackDestination("forgot_password")
    data object Home         : HaztrackDestination("home")
}
```

A `sealed class` is a closed set — you can only have the listed subclasses. This means if you add a new destination later, the compiler will flag every `when` expression that does not handle it.

### `SessionViewModel` — Start Destination on Cold Launch

```kotlin
@HiltViewModel
class SessionViewModel @Inject constructor(authUseCases: AuthUseCases) : ViewModel() {
    val startDestination: String = if (authUseCases.getCurrentUser() != null) {
        HaztrackDestination.Home.route
    } else {
        HaztrackDestination.Login.route
    }
}
```

On every cold launch, `SessionViewModel` checks whether Firebase already has a signed-in user. If yes, the app starts at `Home` and the user never sees the Login screen. This state check is synchronous because `FirebaseAuth.currentUser` reflects the persisted session without a network call.

### `HaztrackNavHost` — The Navigation Graph

```
Login ──► Home (auth screens cleared from back stack)
Login ──► Register
Login ──► ForgotPassword
Register ──► Home (auth screens cleared from back stack)
Register ──► Login (pop back)
ForgotPassword ──► Login (pop back)
Home ──► Login (full back stack cleared on sign-out)
```

**Clearing the auth back stack** is important: after signing in, pressing the Android back button should exit the app, not send the user back to the Login screen. This is achieved with:

```kotlin
navigate(HaztrackDestination.Home.route) {
    popUpTo(HaztrackDestination.Login.route) { inclusive = true }
}
```

`popUpTo(...) { inclusive = true }` removes Login (and everything on the back stack up to and including it) before pushing Home.

---

## 8. Authentication Features

### 8.1 Firebase Email Authentication

**Flow:** `LoginScreen` → `LoginViewModel` → `SignInWithEmailUseCase` → `AuthRepositoryImpl` → `AuthRemoteDataSource` → Firebase Auth SDK.

1. The user types an email and password. Both fields must be non-blank for the "Sign In" button to be enabled (`isSignInEnabled` in `LoginUiState`).
2. On tap, `LoginViewModel.onSignInClick()` is called.
3. The ViewModel sets `isLoading = true` in the state (the button shows a spinner, keyboard input is disabled).
4. `SignInWithEmailUseCase` validates the email format and minimum password length before the network call is made.
5. `AuthRemoteDataSource` calls `firebaseAuth.signInWithEmailAndPassword(email, password).await()`.
6. On success, the ViewModel sends `LoginEvent.NavigateToHome` through the `Channel`.
7. On failure, the exception is mapped to a string resource by `AuthErrorMapper` and displayed below the form.

**Registration** follows the same path but also:
- Requires a "Confirm Password" field.
- The ViewModel checks for a password mismatch **before** calling the use case, so no network call is made if passwords differ.

### 8.2 Google Sign-In

Google Sign-In uses the **AndroidX Credential Manager** API — the modern replacement for the older Google Sign-In SDK. The flow is split between the Screen and the ViewModel by design.

**Why the split?**

`CredentialManager.getCredential()` requires an Activity `Context` to display the Google account picker dialog. ViewModels must **never** hold a reference to an Activity or its Context because the Activity can be destroyed and recreated (e.g. on rotation) while the ViewModel lives on. If the ViewModel held the Activity, it would cause a memory leak and a crash.

The solution:
- `GoogleAuthClient` lives in the **presentation layer** (not injected by Hilt) and handles the Credential Manager call.
- The **Screen composable** creates `GoogleAuthClient`, has access to `LocalContext.current` (the Activity context), and calls `requestIdToken()`.
- The result (just a `String` ID token) is handed to the ViewModel, which then calls the use case with it.

**Step-by-step:**

```
User taps "Continue with Google"
        │
LoginScreen (composable)
  coroutineScope.launch {
    viewModel.onGoogleSignInStarted()          // sets isGoogleSignInLoading = true
    runCatching { googleAuthClient.requestIdToken(context) }
      .onSuccess(viewModel::onGoogleIdTokenReceived)
      .onFailure(viewModel::onGoogleSignInFailed)
  }
        │
GoogleAuthClient.requestIdToken(context)
  Creates CredentialManager
  Builds GetGoogleIdOption with server client ID (from R.string.default_web_client_id)
  Calls credentialManager.getCredential() → shows Google account picker dialog
  Returns the Google ID token String
        │
LoginViewModel.onGoogleIdTokenReceived(idToken)
  calls authUseCases.signInWithGoogle(idToken)
        │
SignInWithGoogleUseCase → AuthRepositoryImpl → AuthRemoteDataSource
  GoogleAuthProvider.getCredential(idToken, null)
  firebaseAuth.signInWithCredential(firebaseCredential).await()
  Returns FirebaseUser → AuthUser
        │
LoginViewModel sends LoginEvent.NavigateToHome
        │
LoginScreen navigates to Home
```

**`R.string.default_web_client_id`** is a string resource automatically generated by the `google-services` Gradle plugin from `google-services.json`. It contains the OAuth 2.0 Web Client ID registered in the Firebase Console. You never write this value manually.

**Cancellation is not an error.** If the user dismisses the Google account picker without selecting an account, a `GetCredentialCancellationException` is thrown. `LoginViewModel.onGoogleSignInFailed` explicitly checks for this and sets `errorMessageRes = null` — no error is shown to the user because they intentionally cancelled.

### 8.3 Password Reset

**Flow:** `ForgotPasswordScreen` → `ForgotPasswordViewModel` → `SendPasswordResetEmailUseCase` → `AuthRepositoryImpl` → `AuthRemoteDataSource` → `firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings).await()`.

1. The user enters their email and taps "Send reset link".
2. `SendPasswordResetEmailUseCase` validates the email format.
3. Firebase sends a password-reset email to the address.
4. On success, `ForgotPasswordUiState.isEmailSent` is set to `true`.
5. The composable replaces the form with a success message and a "Back to sign in" button.

**Note:** Firebase does not reveal whether the email address exists in the system — it always responds with success to prevent email enumeration attacks. This is expected Firebase behaviour.

Password-reset emails use Firebase's in-app action-link mode. The generated hosting link targets
`<PROJECT_ID>.firebaseapp.com/__/auth/links`; `MainActivity` extracts the nested action URL and
passes its `oobCode` to the reset-password navigation destination. Android App Links must be
configured and verified for that Firebase Hosting domain. The reset form then verifies the code
with Firebase before allowing the user to choose a new password. If Haztrack is not installed,
the Firebase Authentication password-reset email template must route to the custom action URL
`https://<PROJECT_ID>.firebaseapp.com/resetPassword`, served by
`public/resetPassword/index.html`. The Firebase Web SDK then provides the same verify-and-confirm
flow in a browser.

The Android App Links association is hosted at
`public/.well-known/assetlinks.json` and deployed with:

```bash
firebase deploy --only hosting
```

The association file currently authorizes the debug APK fingerprint for local emulator testing.
Add the release or Play App Signing fingerprint before distributing a production build.

After deploying Hosting, verify both the association file and browser fallback:

```bash
curl -i https://<PROJECT_ID>.firebaseapp.com/.well-known/assetlinks.json
curl -i https://<PROJECT_ID>.firebaseapp.com/resetPassword/
```

Configure the custom action URL in Firebase Console under Authentication → Templates → the
password-reset email → Customize action URL. The `ActionCodeSettings.url` in the Android data
source remains the continue URL carried inside Firebase's action link.

### 8.4 Session Persistence

Firebase Auth persists the signed-in user to the device's local storage automatically. On the next app launch, `FirebaseAuth.currentUser` is non-null if the session is still valid (tokens not expired, account not deleted).

`SessionViewModel` reads `getCurrentUser()` synchronously at startup and sets `startDestination` to either `"home"` or `"login"`. Because this happens before the first frame is drawn, there is no flash of the wrong screen.

### 8.5 Sign Out

Sign-out must clear two things:
1. **Firebase session** — `firebaseAuth.signOut()` clears the local token.
2. **Credential Manager state** — `CredentialManager.clearCredentialState()` removes the saved Google credential so the account picker is shown again on the next Google Sign-In attempt.

`HomeViewModel.onSignOutClick()` calls `authUseCases.signOut()` (Firebase only). The Screen itself handles the Credential Manager part via `GoogleAuthClient.signOut(context)` because, again, Credential Manager needs a Context.

After sign-out, `HomeEvent.NavigateToLogin` is sent and `HaztrackNavHost` navigates to Login with `popUpTo(0) { inclusive = true }` — this clears the **entire** back stack, so pressing back after sign-out exits the app.

---

## 9. UI Components

Reusable composables shared across screens live in `presentation/components/`.

| Component | Purpose |
|---|---|
| `HaztrackTextField` | Standard outlined text field with label, leading icon, and keyboard options |
| `HaztrackPasswordField` | Password field extending `HaztrackTextField` with a show/hide toggle icon |
| `HaztrackPrimaryButton` | Primary CTA button; shows a `CircularProgressIndicator` when `isLoading = true` |
| `GoogleSignInButton` | Outlined button with the Google logo and "Continue with Google" text |
| `AuthDivider` | A horizontal rule with "OR" text in the centre |
| `AuthTopBar` | A `TopAppBar` with only a back arrow (used on Register and ForgotPassword) |

All user-facing text in these components comes from `res/values/strings.xml` via `stringResource(R.string.xxx)`. Hardcoded strings inside Kotlin files are not used for user-visible text.

Colors are accessed through `MaterialTheme.colorScheme` (e.g., `MaterialTheme.colorScheme.error` for error messages) rather than hardcoded hex values.

---

## 10. Error Handling

`AuthErrorMapper.kt` contains a single extension function on `Throwable`:

```kotlin
fun Throwable.toAuthErrorMessageRes(): Int = when (this) {
    is IllegalArgumentException              -> R.string.auth_error_invalid_input
    is FirebaseAuthWeakPasswordException     -> R.string.auth_error_weak_password
    is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_invalid_credentials
    is FirebaseAuthUserCollisionException    -> R.string.auth_error_account_exists
    is FirebaseAuthInvalidUserException      -> R.string.auth_error_no_account
    is FirebaseNetworkException              -> R.string.auth_error_network
    else                                     -> R.string.auth_error_generic
}
```

**Why return a string resource ID (`Int`) instead of a `String`?**

ViewModels must not hold a reference to `Context` (which is needed to resolve string resources). By returning an `Int` (the resource ID), the ViewModel stays free of Android dependencies. The Composable receives the `Int` from `UiState.errorMessageRes` and resolves the string itself: `stringResource(errorRes)`. This keeps the architecture clean and the ViewModel fully unit-testable without a device.

---

## 11. Build, Lint, and Code Quality

### Building

```bash
./gradlew assembleDebug
```

Produces a debug APK in `app/build/outputs/apk/debug/`. This is the standard build-check command.

### Static Analysis (Detekt)

```bash
./gradlew detekt
```

Detekt is a Kotlin static analysis tool. Rules are configured in `config/detekt/detekt.yml`. It catches code style issues, complexity problems, and potential bugs before they reach review.

### Git Hooks

The `.githooks/` folder contains:
- **`pre-commit`**: Runs lint and detekt checks automatically before every commit. If the checks fail, the commit is rejected.
- **`commit-msg`**: Enforces a consistent commit message format.

To activate the hooks after cloning, run:
```bash
git config core.hooksPath .githooks
```

---

## 12. Setting Up Locally

### Prerequisites

- Android Studio (latest stable) or IntelliJ IDEA with the Android plugin.
- JDK 11+ (the project compiles with Java 11 compatibility).
- A Firebase project with **Email/Password** and **Google** sign-in methods enabled in the Firebase Console.

### Steps

1. **Clone the repository.**

2. **Activate git hooks:**
   ```bash
   git config core.hooksPath .githooks
   ```

3. **Add `google-services.json`.**
   Download `google-services.json` from your Firebase project's settings and place it at `app/google-services.json`. This file is gitignored and must never be committed.

4. **Add the SHA-1 fingerprint to Firebase.**
   Google Sign-In requires your app's SHA-1 signing certificate to be registered in the Firebase Console. For the debug build, run:
   ```bash
   ./gradlew signingReport
   ```
   Copy the `SHA1` value under the `debug` variant and add it in Firebase Console → Project Settings → Your Android App → Add fingerprint.

5. **Sync Gradle and build:**
   ```bash
   ./gradlew assembleDebug
   ```

6. **Run on a device or emulator** with Google Play Services installed (required for Credential Manager / Google Sign-In).

### Key Files Never to Commit

| File | Why |
|---|---|
| `app/google-services.json` | Contains Firebase API keys and OAuth client IDs |
| `local.properties` | Contains your local SDK path; machine-specific |

Both are already listed in `app/.gitignore` and the root `.gitignore`.

---

## 13. Firebase Hosting and Password-Reset Deep Links

The password-reset email uses a Firebase Hosting HTTPS link that Android
recognizes as a verified App Link. `MainActivity` extracts Firebase's
single-use `oobCode`, and the Compose navigation graph opens the
`ResetPasswordScreen`, where Firebase verifies the code and confirms the new
password.

For a beginner-friendly explanation of the complete flow, Hosting setup,
`assetlinks.json`, local deployment, testing, troubleshooting, and security
guidance, see
[Firebase Hosting and Password-Reset Deep Links](deeplinks-firebase-hosting.md).
