package com.danger.haztrack.domain.model

/** Result of a successful image upload to the backend/Cloudinary. */
data class UploadedImage(
    val secureUrl: String,
    val publicId: String,
)
