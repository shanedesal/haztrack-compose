# Haztrack Changelog

All notable feature additions, updates, bug fixes, and functionality changes are recorded here.

## 2026-09-01

### Updated

- Image-upload Timber logs now include `mimeType` and `byteCount` on start, a success line for delete, and failures with `httpCode` when the backend returns an HTTP error. Tokens, image bytes, and `publicId` are still never logged.

### Fixed

- Allowed debug-only cleartext HTTP to the local image-upload backend (`10.0.2.2`, `localhost`, `127.0.0.1`) via `app/src/debug` network security config. Android 9+ otherwise rejects `http://10.0.2.2` with `CLEARTEXT communication ... not permitted`. Release builds are unchanged.
- Planted `Timber.DebugTree()` from `HaztrackApplication` in debug builds so existing Timber calls (including image-upload traces) actually appear in Logcat. Without a planted tree, Timber is a no-op.
- Replaced deprecated `TopAppBarDefaults.centerAlignedTopAppBarColors` in `AuthTopBar` with `topAppBarColors` (Material 3 now uses one colors factory for all top-app-bar variants).
- Replaced the deprecated `Locale(language, country)` constructor in `CountryCodeProvider` with `Locale.Builder().setRegion(...)` (API 21+; `Locale.of` would require API 36).
- Silenced the false Moshi "migrate to KSP" warning emitted by `:app:hiltJavaCompileDebug`. Moshi codegen already runs via KSP; Hilt still mirrors KSP processors onto that javac task (dagger#4116). `app/build.gradle.kts` now strips `moshi-kotlin-codegen` from `hiltJavaCompile*` annotation-processor classpaths only.
- Fixed Detekt `TooManyFunctions` / `ReturnCount` findings on the Profile flow. `TooManyFunctions` now ignores `@Composable` helpers and allows 20 ordinary functions per file/class so form ViewModels can keep one named event per field. `onSaveClick` validation was folded into one `when`, and `toCountryInfoOrNull` now uses a single guard. Also merged a duplicate `complexity:` key that had been dropping the `LongMethod` block.

## 2026-08-31

### Added

- Added a `UserProfile` screen (`presentation/profile/`), reachable by tapping the user card on the Settings screen, showing the signed-in user's first name, last name, email, and profile picture. Accounts signed in with Google show their Google profile photo and a "Signed in with Google" badge.
- Added a Firestore-backed user profile store: a new `users/{uid}` collection holds each user's `firstName`, `lastName`, `email`, and `photoUrl`, with security rules restricting a document to its own user. Added the `firebase-firestore` dependency, `data/remote/api/UserRemoteDataSource.kt`, `data/remote/dto/UserProfileDto.kt`, `data/repository/profile/UserProfileRepositoryImpl.kt`, the `domain/repository/profile/UserProfileRepository` contract, and `domain/model/UserProfile.kt`.
- Added `domain/usecase/profile/` (`GetUserProfileUseCase`, `SaveUserProfileUseCase`, `EnsureUserProfileUseCase`, grouped in `UserProfileUseCases`). `EnsureUserProfileUseCase` self-heals missing profile documents (legacy accounts, first Google sign-in, or a failed write during registration) by deriving a name from the Firebase display name when no explicit first/last name is available.
- Added a reusable `UserAvatar` component (Coil-backed) that renders the user's photo when available and falls back to an initials circle otherwise; used on both Settings and the new Profile screen. Added the `coil-compose` and `coil-network-okhttp` dependencies to support this.
- Added a bottom navigation shell (`MainScaffold`) for the post-login experience with four tabs (Home, My Reports, Notifications, Settings) plus a docked center FAB that opens a new "Report a Hazard" screen.
- Added placeholder `ReportScreen`, `MyReportsScreen`, and `NotificationsScreen` composables with an empty-state message; these screens have no backing data yet.
- Added a `SettingsScreen` with a user info card and the Sign Out action (moved off of Home).
- Added shared `IconBadge`, `EmptyStateMessage`, and `QuickActionCard` composables used across the new screens and the redesigned auth flows.
- Made the Profile screen fully editable: first name, last name, date of birth, gender, and phone number can now be edited inline (Edit/Save/Cancel in the top bar), with the profile picture changeable independently by tapping the avatar. Added `dateOfBirth`, `gender`, `phoneRegionCode`, `phoneDialCode`, and `phoneNumber` fields to `UserProfile`/`UserProfileDto`, new `domain/model/Gender.kt` and `domain/model/PhotoSource.kt` enums, and `domain/usecase/profile/ProfileInputValidation.kt` for the edit form's validation rules.
- Added a custom country-code phone number picker: `presentation/components/PhoneNumberField.kt` (a flag + dial-code chip that opens a searchable bottom sheet) backed by a new `util/CountryCodeProvider.kt`, which builds the country list and validates numbers using the new `io.michaelrocks:libphonenumber-android` dependency. Flags are rendered as Unicode emoji computed from the region code, so no flag image assets were added.
- Added secure profile-picture uploads via a self-hosted backend proxy, so the app never talks to Cloudinary directly and never holds a Cloudinary API secret: new `domain/model/UploadContext.kt`/`UploadedImage.kt` models, `domain/repository/upload/ImageUploadRepository.kt`, `domain/usecase/upload/` (`UploadImageUseCase`, `DeleteUploadedImageUseCase`, grouped in `UploadUseCases`), `data/remote/api/UploadApi.kt` (Retrofit), `data/remote/api/ImageUploadRemoteDataSource.kt`, and `data/repository/upload/ImageUploadRepositoryImpl.kt`. Removing a photo reverts to the linked Google photo (if any) or the initials avatar, and best-effort deletes the orphaned Cloudinary asset.
- Added `presentation/profile/ProfilePhotoPicker.kt` (reads the photo picked via the system Photo Picker and compresses it client-side) and `util/ImageCompression.kt`/`util/IsoDateFormat.kt` helper utilities.
- Added `di/NetworkModule.kt`, providing a shared Retrofit + OkHttp + Moshi stack for calls to our own backend, with an interceptor that automatically attaches the signed-in user's Firebase ID token to every request. This is meant to be reused by future REST endpoints (e.g. hazard reports) rather than each feature building its own HTTP client. Added `di/PhoneNumberModule.kt` to provide the `PhoneNumberUtil` singleton the country picker needs. Added the `retrofit`, `converter-moshi`, `moshi` + `moshi-kotlin-codegen` (via KSP), `okhttp` + `logging-interceptor`, and `libphonenumber-android` dependencies to `gradle/libs.versions.toml`, plus a `BACKEND_BASE_URL` `BuildConfig` field sourced from `local.properties` (never hardcoded).
- Added `docs/backend-image-upload-spec.md`, the specification for the self-hosted Node.js/Express backend that proxies profile-picture uploads to Cloudinary (endpoints, auth model, image-processing pipeline, and security hardening checklist).

### Updated

- Updated email/password registration (`RegisterScreen`) to require First Name and Last Name fields, which are now saved as an explicit Firestore profile document right after account creation (via `SaveUserProfileUseCase`) — previously, registration only collected email and password, leaving the user's name empty everywhere. The combined name is also still saved as the Firebase Auth display name via `FirebaseUser.updateProfile`, for consistency with Google accounts.
- Updated `LoginViewModel` (both email and Google sign-in) to call `EnsureUserProfileUseCase` after a successful sign-in, so accounts created before this feature existed — and Google accounts signing in for the first time — automatically get a Firestore profile document instead of showing blank profile fields.
- Updated the `AuthUser` domain model with `isGoogleAccount` (detected from the Firebase provider data) so the UI can distinguish Google-signed-in users; first/last name now live on the separate `UserProfile` model backed by Firestore rather than on `AuthUser`.
- Updated the Settings screen's user card to be tappable, navigating to the new Profile screen, with a chevron indicating it is a navigation entry.
- Overhauled the app's visual design system: `Type.kt` now defines a full Material 3 type scale, and a new `Shape.kt` adds a consistent rounded-corner scale used by buttons, text fields, and cards. `Color.kt`/`Theme.kt` were extended with additional tonal blue/black surfaces (no gradients).
- Redesigned the Forgot Password and Reset Password screens with large tonal icon badges, grouped card-style form containers, and a back button added to Reset Password for consistency with Forgot Password.
- Refreshed Login and Register screens' spacing and typography to match the new type scale.
- Redesigned Home as a dynamic dashboard: it now greets the signed-in user by name/email and surfaces "Report a Hazard" and "My Reports" quick actions instead of a static welcome message with a sign-out button.
- Enabled edge-to-edge display in `MainActivity` for a more modern, immersive look.
- Changed `SaveUserProfileUseCase` to accept a full `UserProfile` (instead of individual name/email/photo parameters) now that the profile has ~9 fields; updated its `RegisterViewModel` caller accordingly.
- Updated `EnsureUserProfileUseCase` to also seed `photoSource = GOOGLE` the first time a profile document is created for an account with a Google photo, so the app knows it's safe to later replace that photo with an uploaded one.
- Migrated `HomeViewModel`/`HomeUiState` and `SettingsViewModel`/`SettingsUiState` to read the greeting name and Settings user card from the Firestore-backed `UserProfile` (via `UserProfileUseCases`) instead of `AuthUser.displayName`/`photoUrl`, so a name or photo change made on the Profile screen is now reflected everywhere immediately.
- Added an optional trailing `actions` slot to the shared `AuthTopBar` component (used by the Profile screen's Edit/Save/Cancel buttons); existing callers are unaffected since it defaults to empty.

### Documentation

- Added `docs/deeplinks-firebase-hosting.md`, a beginner-focused guide to the password-reset deep-link flow, Firebase Hosting setup, Android App Links verification, deployment, testing, troubleshooting, and security considerations.
- Linked the new guide from `docs/docs.md`.
- Updated `docs/docs.md`: extended the `UserProfile` model/tech-stack/file-structure sections for the new fields and upload layer, rewrote §8.6 (User Profile) and added §8.7 (Editable Profile Fields and Secure Photo Uploads) describing the edit flow and the photo-upload sequence, updated the UI components table, and linked the new `backend-image-upload-spec.md`.

### Fixed

- Added `public/resetPassword/index.html` as the Firebase Hosting browser fallback so desktop users and phones without the app can verify a reset code and set a new password instead of receiving a 404 page.
- Updated the Firebase Hosting and project documentation to describe and verify the browser reset flow.
- Fixed `hiltViewModel()` deprecation warnings raised by `androidx.hilt:hilt-navigation-compose:1.4.0` (unrelated to the Coil/Firestore additions above — this dependency and version predate this feature) by switching all `hiltViewModel()` call sites (`HomeScreen`, `LoginScreen`, `RegisterScreen`, `ForgotPasswordScreen`, `SettingsScreen`, `ProfileScreen`, `HaztrackNavHost`) to import from the new `androidx.hilt.lifecycle.viewmodel.compose` package instead of the deprecated `androidx.hilt.navigation.compose` one. No new Gradle dependency was needed since the new package is already provided transitively by `hilt-navigation-compose:1.4.0`.
- Fixed a detekt `MagicNumber` finding on the Profile screen's Google account badge by extracting the pill-shape corner percent (`50`) into a named `PillShapeCornerPercent` constant.

## 2026-08-30

### Documentation

- Added `.cursor/rules/feature-change-documentation.mdc` to require documentation for future feature additions, feature updates, bug fixes, and functionality changes.
- Established `docs/changelog.md` as the change log.
- Required `docs/docs.md` to remain synchronized when documented features or behavior change.

### Added

- Added a Compose reset-password flow with Firebase reset-code verification, new-password confirmation, validation states, and navigation back to sign-in after success.
- Added Firebase Hosting configuration and public files, including the Android App Links `assetlinks.json` association and its JSON content-type header.

### Updated

- Updated forgot-password emails to use Firebase in-app action links instead of browser-only password resets.
- Updated `MainActivity` and navigation to extract nested Firebase action URLs, capture the `oobCode`, and open the reset-password screen through the verified Hosting link.
