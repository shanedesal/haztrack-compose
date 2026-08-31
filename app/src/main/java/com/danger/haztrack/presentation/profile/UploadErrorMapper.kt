package com.danger.haztrack.presentation.profile

import androidx.annotation.StringRes
import com.danger.haztrack.R
import retrofit2.HttpException
import java.io.IOException

private const val HTTP_PAYLOAD_TOO_LARGE = 413
private const val HTTP_UNSUPPORTED_MEDIA_TYPE = 415
private const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Maps failures from the image-upload backend (see `docs/backend-image-upload-spec.md`) to a
 * user-facing string resource, keeping the ViewModel free of Android [android.content.Context].
 */
@StringRes
fun Throwable.toUploadErrorMessageRes(): Int = when {
    this is HttpException && code() == HTTP_PAYLOAD_TOO_LARGE -> R.string.profile_error_photo_too_large
    this is HttpException && code() == HTTP_UNSUPPORTED_MEDIA_TYPE -> R.string.profile_error_photo_unsupported_type
    this is HttpException && code() == HTTP_TOO_MANY_REQUESTS -> R.string.profile_error_photo_rate_limited
    this is IOException -> R.string.auth_error_network
    else -> R.string.profile_error_photo_upload_failed
}
