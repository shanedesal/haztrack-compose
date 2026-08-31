package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import javax.inject.Inject

/**
 * Guarantees a Firestore profile document exists for the signed-in user, creating one on first
 * use if it's missing. This covers Google sign-in (first run), accounts created before the
 * Firestore profile store existed, and the rare case where the profile write during registration
 * failed but the Firebase Auth account was created successfully.
 *
 * When no explicit name is known (Google sign-in, legacy accounts), the name is derived from
 * [AuthUser.displayName] by splitting it on the first space, since that's the best information
 * Firebase Auth exposes.
 */
class EnsureUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(
        user: AuthUser,
        firstName: String? = null,
        lastName: String? = null,
    ): UserProfile {
        userProfileRepository.getUserProfile(user.id)?.let { return it }

        val (derivedFirstName, derivedLastName) = splitDisplayName(user.displayName)
        val profile = UserProfile(
            id = user.id,
            firstName = firstName?.trim()?.takeIf { it.isNotBlank() } ?: derivedFirstName ?: "",
            lastName = lastName?.trim()?.takeIf { it.isNotBlank() } ?: derivedLastName ?: "",
            email = user.email,
            photoUrl = user.photoUrl,
        )
        userProfileRepository.saveUserProfile(profile)
        return profile
    }

    private fun splitDisplayName(displayName: String?): Pair<String?, String?> {
        val trimmed = displayName?.trim()
        if (trimmed.isNullOrEmpty()) return null to null

        val parts = trimmed.split(" ", limit = 2)
        return parts.getOrNull(0) to parts.getOrNull(1)
    }
}
