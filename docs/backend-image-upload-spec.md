# Backend Image Upload Specification (Node.js + Express)

> This backend is **not part of this repository** — it's a separate service you build and run yourself (locally for now). This document is the contract the Android app already codes against (`di/NetworkModule.kt`, `data/remote/api/UploadApi.kt`), so the app and backend agree on one source of truth. See [`docs.md` §8.7](docs.md#87-editable-profile-fields-and-secure-photo-uploads) for how the Android side uses this.

## 1. Why a backend proxy?

The app never talks to Cloudinary directly and never embeds a Cloudinary API secret. Any secret bundled into an Android app is recoverable by a motivated attacker (APKs can be decompiled), so uploads are proxied through a backend you control: the app sends raw image bytes + a Firebase ID token to your backend; your backend validates/re-encodes the image and pushes it to Cloudinary using server-only credentials. This also gives you a place to enforce rate limits, moderate content, and reuse the exact same pipeline for future upload features (e.g. hazard-report photos) without ever exposing new secrets to the client.

## 2. Auth model

Every request carries `Authorization: Bearer <Firebase ID token>`. The backend verifies it with the **Firebase Admin SDK** (`admin.auth().verifyIdToken(token)`) and derives `uid` **only** from the verified token — never from the request body or path. This single rule is what prevents one user from overwriting or deleting another user's asset: every operation is scoped to the caller's own `uid`, computed server-side.

## 3. Endpoints

Base path: `/api/v1` (matches the Android default `BuildConfig.BACKEND_BASE_URL`).

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| `POST` | `/uploads/:context` | Required | `multipart/form-data`, field `file` | `200 { "secureUrl": "...", "publicId": "haztrack/<context>/<uid>", "context": "profile-picture" }` |
| `DELETE` | `/uploads/:context` | Required | — | `204 No Content` |
| `GET` | `/health` | None | — | `200 { "status": "ok" }` — unauthenticated liveness check for local dev |

`:context` is whitelisted server-side. Today only `profile-picture` is valid; `hazard-report` is reserved for later and should get its own folder, size/dimension limits, and an additional ownership check (e.g. a `reportId` field validated against Firestore) once that feature exists. Reject any other value with `400`.

`public_id` is **always computed server-side** as `haztrack/<context>/<uid>` — never accepted from the client. This means a forged or tampered request can, at worst, overwrite the attacker's own asset, never another user's.

## 4. Processing pipeline (per upload, in order)

1. `helmet()` for security headers.
2. Rate limiting: per-IP, pre-auth (`express-rate-limit`), and per-uid, post-auth (e.g. 20 uploads/hour) — protects both anonymous abuse and a compromised/buggy client hammering the endpoint.
3. Firebase ID token verification (see §2). Reject with `401` if missing/invalid.
4. `multer` with **memory storage only** (never write to disk) — `fileSize` limit (~5MB) and a MIME allowlist (`image/jpeg`, `image/png`, `image/webp`) as a first-pass filter. Reject with `413` (too large) or `415` (unsupported type).
5. `sharp` actually decodes the buffer — this is the real validation step, since it rejects anything that isn't a genuine image regardless of the claimed MIME type or file extension (closes the "renamed `.exe` as `.jpg`" hole). It then:
   - Auto-orients the image (respects EXIF orientation before stripping it).
   - **Strips all EXIF/GPS metadata.**
   - Resizes to fit within e.g. 1024×1024.
   - Re-encodes to JPEG or WebP.
6. Uploads the processed buffer to Cloudinary via `cloudinary.uploader.upload_stream` with:
   - `public_id: haztrack/<context>/<uid>` (deterministic, server-computed — see §3)
   - `overwrite: true`
   - `invalidate: true` (bust any CDN cache of the previous asset at that `public_id`)
   - `resource_type: 'image'`

## 5. Hardening checklist

- Cloudinary `cloud_name` / `api_key` / `api_secret` are read from environment variables only — never returned in any API response, never logged.
- Firebase Admin service-account credentials are environment-only too (`.env`, gitignored; commit a `.env.example` with empty placeholders).
- Deterministic per-user `public_id` (§3) means the worst case of a forged request is a user overwriting their *own* asset.
- Return **generic** error messages to the client (`400` / `401` / `413` / `415` / `429` / `500`); keep full error detail only in server logs. Never log file bytes or the bearer token itself.
- **Local HTTP / cleartext:** debug builds include `app/src/debug/res/xml/network_security_config.xml`, which permits cleartext only to `10.0.2.2` (emulator → host), `localhost`, and `127.0.0.1`. Release builds do not merge that config, so production traffic stays HTTPS-only. A physical device still needs either `adb reverse tcp:4000 tcp:4000` (then point `BACKEND_BASE_URL` at `http://127.0.0.1:4000/api/v1/`) or a TLS tunnel (e.g. ngrok). If you point debug at some other LAN IP, add that host to the debug network-security config — do not enable global cleartext.
- **Forward-looking (not built yet):** for a future "hazard report" context where photos may be evidence and shouldn't be publicly guessable, switch that context's Cloudinary asset `type` to `authenticated` and have the backend mint short-lived signed delivery URLs on read, instead of the public delivery used for profile pictures.

## 6. Suggested project layout

```
backend/
  src/
    config/              # env loading + validation (e.g. a zod schema for required vars)
    middleware/
      auth.middleware.ts       # Firebase ID token verification
      rateLimit.middleware.ts
      upload.middleware.ts     # multer config (memory storage, size/MIME limits)
    modules/uploads/
      uploads.controller.ts
      uploads.service.ts       # sharp processing + Cloudinary upload_stream
      uploadContexts.ts         # per-context folder/limits whitelist
    lib/
      cloudinary.ts
      firebaseAdmin.ts
    app.ts / server.ts
  .env.example
```

## 7. Required environment variables

| Variable | Purpose |
|---|---|
| `PORT` | Port the server listens on (Android default expects `4000`) |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary account identifier |
| `CLOUDINARY_API_KEY` | Cloudinary API key (server-only) |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret (server-only, never sent to the client) |
| `FIREBASE_PROJECT_ID` | Used by the Firebase Admin SDK to verify ID tokens |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Admin service-account credentials (server-only) |
| `MAX_UPLOAD_MB` | Upload size limit enforced by the `multer` middleware |

## 8. Android-side contract summary

- Base URL: `BuildConfig.BACKEND_BASE_URL`, sourced from `local.properties` (`BACKEND_BASE_URL=...`), defaulting to `http://10.0.2.2:4000/api/v1/` (the Android emulator's alias for the host machine).
- Every request automatically carries `Authorization: Bearer <Firebase ID token>` via `NetworkModule`'s OkHttp interceptor — the app never attaches it manually.
- On a successful profile-picture upload, the app immediately persists `photoUrl = secureUrl` and `photoSource = CLOUDINARY` to Firestore (independent of any other pending profile edits).
- On `DELETE`, the app treats a failure as non-fatal (logged only) so removing/replacing a photo locally is never blocked by a flaky backend.
