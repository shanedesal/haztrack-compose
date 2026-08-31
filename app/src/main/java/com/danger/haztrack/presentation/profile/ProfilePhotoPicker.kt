package com.danger.haztrack.presentation.profile

import android.content.Context
import android.net.Uri
import com.danger.haztrack.util.compressImageForUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads a photo picked via the system Photo Picker and compresses it off the main thread. Lives
 * in the presentation layer (not injected by Hilt, not held by the ViewModel) because reading a
 * `content://` Uri needs a [Context] — the same reasoning as
 * [com.danger.haztrack.presentation.auth.common.GoogleAuthClient].
 */
class ProfilePhotoPicker {
    suspend fun readAndCompress(context: Context, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openInputStream(uri)
                ?: error("Could not open the picked image")
            compressImageForUpload(stream)
        }
    }

    companion object {
        const val UPLOAD_MIME_TYPE = "image/jpeg"
    }
}
