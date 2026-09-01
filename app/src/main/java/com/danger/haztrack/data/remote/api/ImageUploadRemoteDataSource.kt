package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UploadResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ImageUploadRemoteDataSource @Inject constructor(
    private val uploadApi: UploadApi,
) {
    suspend fun upload(context: String, bytes: ByteArray, mimeType: String): UploadResponseDto {
        Timber.d("UploadImage started: context=$context, mimeType=$mimeType, byteCount=${bytes.size}")
        val extension = mimeType.substringAfterLast('/', missingDelimiterValue = "jpg")
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "$context.$extension",
            body = requestBody,
        )
        return runCatching { uploadApi.upload(context, part) }
            .onSuccess { Timber.d("UploadImage succeeded: context=$context") }
            .onFailure { throwable -> logUploadFailure("UploadImage", context, throwable) }
            .getOrThrow()
    }

    suspend fun delete(context: String) {
        Timber.d("DeleteUploadedImage started: context=$context")
        runCatching { uploadApi.delete(context) }
            .onSuccess { Timber.d("DeleteUploadedImage succeeded: context=$context") }
            .onFailure { throwable -> logUploadFailure("DeleteUploadedImage", context, throwable) }
            .getOrThrow()
    }

    private fun logUploadFailure(operation: String, context: String, throwable: Throwable) {
        if (throwable is CancellationException) return
        val httpCode = (throwable as? HttpException)?.code()
        if (httpCode != null) {
            Timber.e(throwable, "$operation failed: context=$context, httpCode=$httpCode")
        } else {
            Timber.e(throwable, "$operation failed: context=$context")
        }
    }
}
