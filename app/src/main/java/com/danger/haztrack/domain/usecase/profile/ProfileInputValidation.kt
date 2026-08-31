package com.danger.haztrack.domain.usecase.profile

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Pure validation rules for the profile edit form, mirroring the style of
 * `domain/usecase/auth/AuthInputValidation.kt`. Kept as plain boolean checks (rather than
 * `require`-and-throw) since the ViewModel needs to pick a distinct, field-specific error
 * message for each failure instead of one generic "invalid input" message.
 */
object ProfileInputValidation {
    private const val ISO_DATE_PATTERN = "yyyy-MM-dd"

    fun isNameValid(value: String): Boolean = value.isNotBlank()

    /** A blank/null date of birth is allowed (the field is optional); only rejects future dates. */
    fun isDateOfBirthValid(isoDate: String?): Boolean {
        if (isoDate.isNullOrBlank()) return true
        return isoDate <= todayIso()
    }

    private fun todayIso(): String {
        val formatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(System.currentTimeMillis())
    }
}
