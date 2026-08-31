package com.danger.haztrack.domain.model

/**
 * Identifies which image-upload endpoint/whitelisted context on the backend an upload belongs
 * to. The backend uses this to pick a Cloudinary folder, size/dimension limits, and ownership
 * rules for the asset (see `docs/backend-image-upload-spec.md`).
 *
 * New features that need image uploads (e.g. hazard reports) add a case here instead of
 * introducing a new upload pipeline.
 */
enum class UploadContext(val pathSegment: String) {
    PROFILE_PICTURE("profile-picture"),
}
