package com.danger.haztrack.data.repository.profile

import com.danger.haztrack.data.remote.api.UserRemoteDataSource
import com.danger.haztrack.data.remote.dto.UserProfileDto
import com.danger.haztrack.domain.model.Gender
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserProfileRepository {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return runCatching { userRemoteDataSource.getUserProfile() }
            .onFailure { throwable ->
                if(throwable !is HttpException || throwable.code() != HttpURLConnection.HTTP_NOT_FOUND) {
                    Timber.e(throwable, "GetUserProfile failed")
                }
            }
            .getOrNull()
            ?.toUserProfile(userId)
            .also { _userProfile.value = it }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        val dto = profile.toDto()
        Timber.d("saveUserProfile request: photoUrl=${dto.photoUrl}, photoSource=${dto.photoSource}") // TEMP
        val saved = userRemoteDataSource.saveUserProfile(dto)
        _userProfile.value = saved.toUserProfile(profile.id)
    }

    override fun clearCachedProfile() {
        _userProfile.value = null
    }

    private fun UserProfileDto.toUserProfile(userId: String): UserProfile {
        return UserProfile(
            id = userId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            photoUrl = photoUrl,
            photoSource = photoSource.toPhotoSourceOrDefault(),
            dateOfBirth = dateOfBirth,
            gender = gender.toGenderOrNull(),
            phoneRegionCode = phoneRegionCode,
            phoneDialCode = phoneDialCode,
            phoneNumber = phoneNumber,
        )
    }

    private fun UserProfile.toDto(): UserProfileDto {
        return UserProfileDto(
            firstName = firstName,
            lastName = lastName,
            email = email,
            photoUrl = photoUrl,
            photoSource = photoSource.name,
            dateOfBirth = dateOfBirth,
            gender = gender?.name,
            phoneRegionCode = phoneRegionCode,
            phoneDialCode = phoneDialCode,
            phoneNumber = phoneNumber,
        )
    }

    private fun String?.toPhotoSourceOrDefault(): PhotoSource {
        return this?.let { value ->
            runCatching { PhotoSource.valueOf(value) }.getOrNull()
        } ?: PhotoSource.NONE
    }

    private fun String?.toGenderOrNull(): Gender? {
        return this?.let { value -> runCatching { Gender.valueOf(value) }.getOrNull() }
    }
}
