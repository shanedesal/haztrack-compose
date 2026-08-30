# Haztrack Changelog

All notable feature additions, updates, bug fixes, and functionality changes are recorded here.

## 2026-08-30

### Documentation

- Added `.cursor/rules/feature-change-documentation.mdc` to require documentation for future feature additions, feature updates, bug fixes, and functionality changes.
- Established `docs/changelog.md` as the change log.
- Required `docs/docs.md` to remain synchronized when documented features or behavior change.

### Fixed

- Completed password-reset deep-link handling by passing Firebase action-code settings, parsing nested Firebase action URLs, and rendering the reset-password screen.
- The reset flow now opens the app-linked password reset form when Android App Links are verified for the Firebase Hosting domain.
- Added the Firebase Hosting `assetlinks.json` association for the debug emulator APK and configured its JSON content type.
