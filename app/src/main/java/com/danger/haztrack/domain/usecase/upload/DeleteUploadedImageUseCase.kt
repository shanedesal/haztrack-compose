package com.danger.haztrack.domain.usecase.upload

import com.danger.haztrack.domain.model.UploadContext
import com.danger.haztrack.domain.repository.upload.ImageUploadRepository
import javax.inject.Inject

class DeleteUploadedImageUseCase @Inject constructor(
    private val imageUploadRepository: ImageUploadRepository,
) {
    suspend operator fun invoke(context: UploadContext) {
        imageUploadRepository.delete(context)
    }
}
