# Haztrack Changelog

All notable feature additions, updates, bug fixes, and functionality changes are recorded here.

## 2026-08-31

### Documentation

- Added `docs/deeplinks-firebase-hosting.md`, a beginner-focused guide to the password-reset deep-link flow, Firebase Hosting setup, Android App Links verification, deployment, testing, troubleshooting, and security considerations.
- Linked the new guide from `docs/docs.md`.

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
