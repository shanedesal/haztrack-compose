package com.danger.haztrack.domain.model

/**
 * Where a [UserProfile.photoUrl] currently points to, so the app knows whether it's safe to
 * overwrite/delete the underlying asset (a Cloudinary asset we own) or must leave it alone (a
 * Google-hosted photo).
 */
enum class PhotoSource {
    NONE,
    GOOGLE,
    CLOUDINARY,
}
