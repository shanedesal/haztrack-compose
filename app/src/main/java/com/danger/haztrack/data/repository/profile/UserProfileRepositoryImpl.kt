package com.danger.haztrack.data.repository.profile

import com.danger.haztrack.data.remote.api.UserRemoteDataSource
import com.danger.haztrack.data.remote.dto.UserProfileDto
import com.danger.haztrack.domain.model.Gender
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserProfileRepository {

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return runCatching { userRemoteDataSource.getUserProfile(userId) }
            .onFailure { Timber.e(it, "GetUserProfile failed") }
            .getOrNull()
            ?.toUserProfile(userId)
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        userRemoteDataSource.saveUserProfile(profile.id, profile.toDto())
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
