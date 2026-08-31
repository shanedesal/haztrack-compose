package com.danger.haztrack.domain.usecase.upload

import javax.inject.Inject

data class UploadUseCases @Inject constructor(
    val uploadImage: UploadImageUseCase,
    val deleteUploadedImage: DeleteUploadedImageUseCase,
)
