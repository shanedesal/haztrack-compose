package com.danger.haztrack.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Conversions between the ISO-8601 `yyyy-MM-dd` strings `UserProfile.dateOfBirth` is stored as
 * and the UTC epoch-millis the Material 3 `DatePicker` works with. Using `SimpleDateFormat`
 * (rather than `java.time`) avoids needing core-library desugaring on minSdk 24.
 */
private const val ISO_DATE_PATTERN = "yyyy-MM-dd"
private const val DISPLAY_DATE_PATTERN = "MMM d, yyyy"

private fun utcFormatter(pattern: String): SimpleDateFormat {
    return SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
}

fun epochMillisToIsoDate(epochMillis: Long): String = utcFormatter(ISO_DATE_PATTERN).format(epochMillis)

fun isoDateToEpochMillis(isoDate: String): Long? {
    return runCatching { utcFormatter(ISO_DATE_PATTERN).parse(isoDate)?.time }.getOrNull()
}

fun formatIsoDateForDisplay(isoDate: String): String {
    val millis = isoDateToEpochMillis(isoDate) ?: return isoDate
    return utcFormatter(DISPLAY_DATE_PATTERN).format(millis)
}
