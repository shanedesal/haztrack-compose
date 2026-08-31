package com.danger.haztrack.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.R
import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.model.Gender
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.domain.model.UploadContext
import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.domain.usecase.profile.ProfileInputValidation
import com.danger.haztrack.domain.usecase.profile.UserProfileUseCases
import com.danger.haztrack.domain.usecase.upload.UploadUseCases
import com.danger.haztrack.util.CountryCodeProvider
import com.danger.haztrack.util.CountryInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
    private val uploadUseCases: UploadUseCases,
    private val countryCodeProvider: CountryCodeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var authUser: AuthUser? = null
    private var savedProfile: UserProfile? = null

    init {
        _uiState.update { it.copy(countries = countryCodeProvider.countries) }
        loadProfile()
    }

    private fun loadProfile() {
        val user = authUseCases.getCurrentUser()
        authUser = user
        if (user == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update {
            it.copy(email = user.email, photoUrl = user.photoUrl, isGoogleAccount = user.isGoogleAccount)
        }

        viewModelScope.launch {
            // ensureUserProfile self-heals: it creates the Firestore document on the fly for
            // Google/legacy accounts or a failed registration write, instead of showing blanks.
            runCatching { userProfileUseCases.ensureUserProfile(user) }
                .onSuccess { profile ->
                    savedProfile = profile
                    _uiState.update { it.copyFromProfile(profile, isLoading = false) }
                }
                .onFailure { _uiState.update { state -> state.copy(isLoading = false) } }
        }
    }

    fun onEditClick() {
        _uiState.update { state ->
            val country = state.selectedCountry ?: countryCodeProvider.defaultCountry()
            state.copy(
                isEditing = true,
                errorMessageRes = null,
                selectedCountry = country,
                phoneRegionCode = state.phoneRegionCode ?: country.regionCode,
                phoneDialCode = state.phoneDialCode ?: country.dialCode,
            )
        }
    }

    fun onCancelClick() {
        val profile = savedProfile
        _uiState.update { state ->
            if (profile != null) state.copyFromProfile(profile, isEditing = false) else state.copy(isEditing = false)
        }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, errorMessageRes = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessageRes = null) }
    }

    fun onDateOfBirthChange(isoDate: String) {
        _uiState.update { it.copy(dateOfBirth = isoDate, errorMessageRes = null) }
    }

    fun onGenderChange(gender: Gender) {
        _uiState.update { it.copy(gender = gender, errorMessageRes = null) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessageRes = null) }
    }

    fun onCountrySelected(country: CountryInfo) {
        _uiState.update {
            it.copy(
                selectedCountry = country,
                phoneRegionCode = country.regionCode,
                phoneDialCode = country.dialCode,
                errorMessageRes = null,
            )
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        val userId = authUser?.id ?: savedProfile?.id ?: return
        val errorRes = validationErrorRes(state)
        if (errorRes != null) {
            _uiState.update { it.copy(errorMessageRes = errorRes) }
            return
        }

        val updatedProfile = UserProfile(
            id = userId,
            firstName = state.firstName,
            lastName = state.lastName,
            email = state.email,
            photoUrl = state.photoUrl,
            photoSource = state.photoSource,
            dateOfBirth = state.dateOfBirth,
            gender = state.gender,
            phoneRegionCode = state.phoneRegionCode,
            phoneDialCode = state.phoneDialCode,
            phoneNumber = state.phoneNumber.takeIf { it.isNotBlank() },
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessageRes = null) }
            runCatching { userProfileUseCases.saveUserProfile(updatedProfile) }
                .onSuccess {
                    savedProfile = updatedProfile
                    _uiState.update { it.copyFromProfile(updatedProfile, isSaving = false, isEditing = false) }
                }
                .onFailure { throwable ->
                    Timber.e(throwable, "SaveUserProfile failed")
                    _uiState.update { it.copy(isSaving = false, errorMessageRes = R.string.profile_error_save_failed) }
                }
        }
    }

    private fun validationErrorRes(state: ProfileUiState): Int? {
        val regionCode = state.phoneRegionCode
        return when {
            !ProfileInputValidation.isNameValid(state.firstName) ||
                !ProfileInputValidation.isNameValid(state.lastName) ->
                R.string.profile_error_name_required
            !ProfileInputValidation.isDateOfBirthValid(state.dateOfBirth) ->
                R.string.profile_error_future_date_of_birth
            state.phoneNumber.isNotBlank() &&
                (regionCode == null || !countryCodeProvider.isValidNumber(regionCode, state.phoneNumber)) ->
                R.string.profile_error_invalid_phone_number
            else -> null
        }
    }

    fun onPhotoPicked(bytes: ByteArray, mimeType: String) {
        if (authUser?.id == null && savedProfile?.id == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, photoErrorMessageRes = null) }
            runCatching { uploadUseCases.uploadImage(UploadContext.PROFILE_PICTURE, bytes, mimeType) }
                .onSuccess { uploaded -> persistPhoto(uploaded.secureUrl, PhotoSource.CLOUDINARY) }
                .onFailure { throwable ->
                    Timber.e(throwable, "UploadProfilePicture failed")
                    _uiState.update {
                        it.copy(isUploadingPhoto = false, photoErrorMessageRes = throwable.toUploadErrorMessageRes())
                    }
                }
        }
    }

    fun onRemovePhotoClick() {
        val currentSource = _uiState.value.photoSource
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, photoErrorMessageRes = null) }
            if (currentSource == PhotoSource.CLOUDINARY) {
                runCatching { uploadUseCases.deleteUploadedImage(UploadContext.PROFILE_PICTURE) }
                    .onFailure { Timber.w(it, "DeleteUploadedImage failed; still clearing the local profile photo") }
            }
            val fallbackUrl = authUser?.photoUrl
            val fallbackSource = if (fallbackUrl != null) PhotoSource.GOOGLE else PhotoSource.NONE
            persistPhoto(fallbackUrl, fallbackSource)
        }
    }

    private suspend fun persistPhoto(photoUrl: String?, photoSource: PhotoSource) {
        val userId = authUser?.id ?: savedProfile?.id ?: return
        val baseline = savedProfile ?: UserProfile(
            id = userId,
            firstName = _uiState.value.firstName,
            lastName = _uiState.value.lastName,
            email = _uiState.value.email,
            photoUrl = null,
        )
        val updatedProfile = baseline.copy(photoUrl = photoUrl, photoSource = photoSource)
        runCatching { userProfileUseCases.saveUserProfile(updatedProfile) }
            .onSuccess {
                savedProfile = updatedProfile
                _uiState.update {
                    it.copy(isUploadingPhoto = false, photoUrl = photoUrl, photoSource = photoSource)
                }
            }
            .onFailure { throwable ->
                Timber.e(throwable, "SaveUserProfile (photo) failed")
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        photoErrorMessageRes = R.string.profile_error_save_failed
                    )
                }
            }
    }

    private fun ProfileUiState.copyFromProfile(
        profile: UserProfile,
        isLoading: Boolean = this.isLoading,
        isEditing: Boolean = this.isEditing,
        isSaving: Boolean = this.isSaving,
    ): ProfileUiState {
        return copy(
            isLoading = isLoading,
            isEditing = isEditing,
            isSaving = isSaving,
            firstName = profile.firstName,
            lastName = profile.lastName,
            email = profile.email ?: email,
            photoUrl = profile.photoUrl ?: photoUrl,
            photoSource = profile.photoSource,
            dateOfBirth = profile.dateOfBirth,
            gender = profile.gender,
            phoneRegionCode = profile.phoneRegionCode,
            phoneDialCode = profile.phoneDialCode,
            phoneNumber = profile.phoneNumber.orEmpty(),
            selectedCountry = countryCodeProvider.findByRegionCode(profile.phoneRegionCode),
            errorMessageRes = null,
        )
    }
}
