# Haztrack Changelog

All notable feature additions, updates, bug fixes, and functionality changes are recorded here.

## 2026-08-31

### Added

- Added a bottom navigation shell (`MainScaffold`) for the post-login experience with four tabs (Home, My Reports, Notifications, Settings) plus a docked center FAB that opens a new "Report a Hazard" screen.
- Added placeholder `ReportScreen`, `MyReportsScreen`, and `NotificationsScreen` composables with an empty-state message; these screens have no backing data yet.
- Added a `SettingsScreen` with a user info card and the Sign Out action (moved off of Home).
- Added shared `IconBadge`, `EmptyStateMessage`, and `QuickActionCard` composables used across the new screens and the redesigned auth flows.

### Updated

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
