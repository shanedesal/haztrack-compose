package com.danger.haztrack.data.repository.upload

import com.danger.haztrack.data.remote.api.ImageUploadRemoteDataSource
import com.danger.haztrack.domain.model.UploadContext
import com.danger.haztrack.domain.model.UploadedImage
import com.danger.haztrack.domain.repository.upload.ImageUploadRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadRepositoryImpl @Inject constructor(
    private val imageUploadRemoteDataSource: ImageUploadRemoteDataSource,
) : ImageUploadRepository {

    override suspend fun upload(context: UploadContext, bytes: ByteArray, mimeType: String): UploadedImage {
        val response = imageUploadRemoteDataSource.upload(context.pathSegment, bytes, mimeType)
        return UploadedImage(secureUrl = response.secureUrl, publicId = response.publicId)
    }

    override suspend fun delete(context: UploadContext) {
        // Best-effort cleanup: if the backend delete fails (e.g. offline), the profile is still
        // updated locally/in Firestore by the caller — we don't want a dangling Cloudinary asset
        // to block the user from removing their photo.
        runCatching { imageUploadRemoteDataSource.delete(context.pathSegment) }
            .onFailure { Timber.w(it, "DeleteUploadedImage failed for context=${context.pathSegment}") }
    }
}
