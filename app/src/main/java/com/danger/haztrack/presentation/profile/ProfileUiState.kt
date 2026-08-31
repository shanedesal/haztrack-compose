package com.danger.haztrack.presentation.profile

import androidx.annotation.StringRes
import com.danger.haztrack.domain.model.Gender
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.util.CountryInfo

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isGoogleAccount: Boolean = false,
    val email: String? = null,
    val photoUrl: String? = null,
    val photoSource: PhotoSource = PhotoSource.NONE,
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val phoneRegionCode: String? = null,
    val phoneDialCode: String? = null,
    val phoneNumber: String = "",
    val selectedCountry: CountryInfo? = null,
    val countries: List<CountryInfo> = emptyList(),
    @StringRes val errorMessageRes: Int? = null,
    @StringRes val photoErrorMessageRes: Int? = null,
)
