package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UploadResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadRemoteDataSource @Inject constructor(
    private val uploadApi: UploadApi,
) {
    suspend fun upload(context: String, bytes: ByteArray, mimeType: String): UploadResponseDto {
        Timber.d("UploadImage started: context=$context")
        val extension = mimeType.substringAfterLast('/', missingDelimiterValue = "jpg")
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "$context.$extension",
            body = requestBody,
        )
        return uploadApi.upload(context, part).also {
            Timber.d("UploadImage succeeded: context=$context")
        }
    }

    suspend fun delete(context: String) {
        Timber.d("DeleteUploadedImage started: context=$context")
        uploadApi.delete(context)
    }
}
