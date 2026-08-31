package com.danger.haztrack.data.remote.dto

import com.squareup.moshi.JsonClass

/** Backend response body for a successful image upload — see `docs/backend-image-upload-spec.md`. */
@JsonClass(generateAdapter = true)
data class UploadResponseDto(
    val secureUrl: String,
    val publicId: String,
    val context: String,
)
