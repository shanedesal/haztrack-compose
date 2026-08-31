# Haztrack Changelog

All notable feature additions, updates, bug fixes, and functionality changes are recorded here.

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

### Documentation

- Added `docs/deeplinks-firebase-hosting.md`, a beginner-focused guide to the password-reset deep-link flow, Firebase Hosting setup, Android App Links verification, deployment, testing, troubleshooting, and security considerations.
- Linked the new guide from `docs/docs.md`.

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
