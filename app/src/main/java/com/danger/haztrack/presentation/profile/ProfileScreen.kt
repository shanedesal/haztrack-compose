package com.danger.haztrack.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.domain.model.Gender
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.presentation.components.AuthTopBar
import com.danger.haztrack.presentation.components.HaztrackTextField
import com.danger.haztrack.presentation.components.PhoneNumberField
import com.danger.haztrack.presentation.components.UserAvatar
import com.danger.haztrack.util.CountryInfo
import com.danger.haztrack.util.epochMillisToIsoDate
import com.danger.haztrack.util.formatIsoDateForDisplay
import com.danger.haztrack.util.isoDateToEpochMillis
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PILL_SHAPE_CORNER_PERCENTAGE = 50
private val AvatarSize = 112.dp
private val AvatarBadgeSize = 32.dp

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoPicker = remember { ProfilePhotoPicker() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching { photoPicker.readAndCompress(context, uri) }
                    .onSuccess { bytes -> viewModel.onPhotoPicked(bytes, ProfilePhotoPicker.UPLOAD_MIME_TYPE) }
                    .onFailure { Timber.e(it, "Reading picked profile photo failed") }
            }
        }
    }

    ProfileContent(
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onEditClick = viewModel::onEditClick,
        onCancelClick = viewModel::onCancelClick,
        onSaveClick = viewModel::onSaveClick,
        onFirstNameChange = viewModel::onFirstNameChange,
        onLastNameChange = viewModel::onLastNameChange,
        onDateOfBirthChange = viewModel::onDateOfBirthChange,
        onGenderChange = viewModel::onGenderChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onCountrySelected = viewModel::onCountrySelected,
        onChangePhotoClick = {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onRemovePhotoClick = viewModel::onRemovePhotoClick,
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCountrySelected: (CountryInfo) -> Unit,
    onChangePhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AuthTopBar(
                onNavigateBack = onNavigateBack,
                title = stringResource(R.string.profile_title),
                actions = {
                    if (!uiState.isLoading) {
                        ProfileTopBarActions(
                            isEditing = uiState.isEditing,
                            isSaving = uiState.isSaving,
                            onEditClick = onEditClick,
                            onCancelClick = onCancelClick,
                            onSaveClick = onSaveClick,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val fallback = stringResource(R.string.profile_name_fallback)
        val initial = (uiState.firstName.takeIf { it.isNotBlank() } ?: uiState.email)
            ?.trim()?.firstOrNull()?.uppercase() ?: "?"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProfileAvatar(
                photoUrl = uiState.photoUrl,
                initial = initial,
                isUploading = uiState.isUploadingPhoto,
                onChangePhotoClick = onChangePhotoClick,
            )

            if (uiState.isGoogleAccount) {
                Spacer(modifier = Modifier.height(16.dp))
                GoogleAccountBadge()
            }

            if (uiState.photoSource == PhotoSource.CLOUDINARY) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onRemovePhotoClick, enabled = !uiState.isUploadingPhoto) {
                    Text(text = stringResource(R.string.profile_remove_photo_button))
                }
            }

            uiState.photoErrorMessageRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (uiState.isEditing) {
                        ProfileEditFields(
                            uiState = uiState,
                            onFirstNameChange = onFirstNameChange,
                            onLastNameChange = onLastNameChange,
                            onDateOfBirthChange = onDateOfBirthChange,
                            onGenderChange = onGenderChange,
                            onPhoneNumberChange = onPhoneNumberChange,
                            onCountrySelected = onCountrySelected,
                        )
                    } else {
                        ProfileReadOnlyFields(uiState = uiState, fallback = fallback)
                    }
                }
            }

            uiState.errorMessageRes?.let { errorRes ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTopBarActions(
    isEditing: Boolean,
    isSaving: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    if (isEditing) {
        TextButton(onClick = onCancelClick, enabled = !isSaving) {
            Text(text = stringResource(R.string.profile_cancel_button))
        }
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            TextButton(onClick = onSaveClick) {
                Text(text = stringResource(R.string.profile_save_button))
            }
        }
    } else {
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.profile_edit_button_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    initial: String,
    isUploading: Boolean,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(AvatarSize), contentAlignment = Alignment.Center) {
        UserAvatar(
            photoUrl = photoUrl,
            initial = initial,
            size = AvatarSize,
            textStyle = MaterialTheme.typography.headlineMedium,
        )
        if (isUploading) {
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(AvatarBadgeSize)
                    .clickable(onClick = onChangePhotoClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = stringResource(R.string.profile_change_photo_description),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileReadOnlyFields(uiState: ProfileUiState, fallback: String) {
    ProfileField(
        label = stringResource(R.string.profile_first_name_label),
        value = uiState.firstName.ifBlank { fallback }
    )
    FieldDivider()
    ProfileField(
        label = stringResource(R.string.profile_last_name_label),
        value = uiState.lastName.ifBlank { fallback }
    )
    FieldDivider()
    ProfileField(
        label = stringResource(R.string.profile_email_label),
        value = uiState.email?.takeIf { it.isNotBlank() } ?: fallback,
    )
    FieldDivider()
    ProfileField(
        label = stringResource(R.string.profile_date_of_birth_label),
        value = uiState.dateOfBirth?.let(::formatIsoDateForDisplay) ?: fallback,
    )
    FieldDivider()
    ProfileField(
        label = stringResource(R.string.profile_gender_label),
        value = uiState.gender?.displayLabel() ?: fallback,
    )
    FieldDivider()
    ProfileField(
        label = stringResource(R.string.profile_phone_number_label),
        value = uiState.phoneNumber.takeIf { it.isNotBlank() }
            ?.let { number -> "${uiState.phoneDialCode.orEmpty()} $number".trim() }
            ?: fallback,
    )
}

@Composable
private fun ProfileEditFields(
    uiState: ProfileUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCountrySelected: (CountryInfo) -> Unit,
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }

    HaztrackTextField(
        value = uiState.firstName,
        onValueChange = onFirstNameChange,
        label = stringResource(R.string.profile_first_name_label),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
    )
    Spacer(modifier = Modifier.height(16.dp))
    HaztrackTextField(
        value = uiState.lastName,
        onValueChange = onLastNameChange,
        label = stringResource(R.string.profile_last_name_label),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
    )
    Spacer(modifier = Modifier.height(16.dp))
    ProfileField(label = stringResource(R.string.profile_email_label), value = uiState.email.orEmpty())
    Spacer(modifier = Modifier.height(16.dp))

    DateOfBirthField(
        dateOfBirth = uiState.dateOfBirth,
        onClick = { isDatePickerVisible = true },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))

    GenderDropdownField(
        selected = uiState.gender,
        onSelected = onGenderChange,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))

    PhoneNumberField(
        countries = uiState.countries,
        selectedCountry = uiState.selectedCountry,
        phoneNumber = uiState.phoneNumber,
        onCountrySelected = onCountrySelected,
        onPhoneNumberChange = onPhoneNumberChange,
        label = stringResource(R.string.profile_phone_number_label),
        modifier = Modifier.fillMaxWidth(),
    )

    if (isDatePickerVisible) {
        DateOfBirthPickerDialog(
            initialIsoDate = uiState.dateOfBirth,
            onDismiss = { isDatePickerVisible = false },
            onConfirm = { isoDate ->
                onDateOfBirthChange(isoDate)
                isDatePickerVisible = false
            },
        )
    }
}

@Composable
private fun DateOfBirthField(dateOfBirth: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateOfBirth?.let(::formatIsoDateForDisplay).orEmpty(),
            onValueChange = {},
            label = { Text(text = stringResource(R.string.profile_date_of_birth_label)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            trailingIcon = {
                Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        // The field above is intentionally disabled (read-only display); this transparent
        // overlay is what actually captures the tap to open the date picker dialog.
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPickerDialog(initialIsoDate: String?, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val initialMillis = initialIsoDate?.let(::isoDateToEpochMillis)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> onConfirm(epochMillisToIsoDate(millis)) }
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.profile_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_date_picker_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdownField(selected: Gender?, onSelected: (Gender) -> Unit, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.displayLabel().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.profile_gender_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            Gender.entries.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(text = gender.displayLabel()) },
                    onClick = {
                        onSelected(gender)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun Gender.displayLabel(): String = when (this) {
    Gender.MALE -> stringResource(R.string.profile_gender_male)
    Gender.FEMALE -> stringResource(R.string.profile_gender_female)
    Gender.OTHER -> stringResource(R.string.profile_gender_other)
    Gender.PREFER_NOT_TO_SAY -> stringResource(R.string.profile_gender_prefer_not_to_say)
}

@Composable
private fun FieldDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun ProfileField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GoogleAccountBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PILL_SHAPE_CORNER_PERCENTAGE),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = stringResource(R.string.auth_google_logo_content_description),
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.profile_google_account_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
