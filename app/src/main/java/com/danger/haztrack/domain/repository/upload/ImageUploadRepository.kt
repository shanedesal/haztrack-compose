package com.danger.haztrack.domain.repository.upload

import com.danger.haztrack.domain.model.UploadContext
import com.danger.haztrack.domain.model.UploadedImage

interface ImageUploadRepository {
    suspend fun upload(context: UploadContext, bytes: ByteArray, mimeType: String): UploadedImage

    suspend fun delete(context: UploadContext)
}
