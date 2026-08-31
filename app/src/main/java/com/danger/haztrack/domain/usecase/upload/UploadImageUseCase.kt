package com.danger.haztrack.domain.usecase.upload

import com.danger.haztrack.domain.model.UploadContext
import com.danger.haztrack.domain.model.UploadedImage
import com.danger.haztrack.domain.repository.upload.ImageUploadRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val imageUploadRepository: ImageUploadRepository,
) {
    suspend operator fun invoke(context: UploadContext, bytes: ByteArray, mimeType: String): UploadedImage {
        return imageUploadRepository.upload(context, bytes, mimeType)
    }
}
