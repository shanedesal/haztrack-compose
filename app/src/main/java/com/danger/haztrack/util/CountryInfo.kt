package com.danger.haztrack.util

/** A country entry for the phone number country-code picker. */
data class CountryInfo(
    val regionCode: String,
    val dialCode: String,
    val displayName: String,
    val flagEmoji: String,
)
