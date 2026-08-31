package com.danger.haztrack.util

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the list of countries (region code, dial code, display name, flag) shown by
 * `PhoneNumberField`, and validates a phone number for a given region — all backed by
 * [PhoneNumberUtil] so we don't have to hand-maintain a country/dial-code table.
 *
 * The flag is rendered as a Unicode "regional indicator symbol" emoji computed from the
 * 2-letter ISO region code, so no flag image assets are needed.
 */
@Singleton
class CountryCodeProvider @Inject constructor(
    private val phoneNumberUtil: PhoneNumberUtil,
) {
    val countries: List<CountryInfo> by lazy { buildCountries() }

    fun defaultCountry(): CountryInfo {
        val deviceRegion = Locale.getDefault().country.uppercase(Locale.ROOT)
        return countries.firstOrNull { it.regionCode == deviceRegion }
            ?: countries.firstOrNull { it.regionCode == FALLBACK_REGION_CODE }
            ?: countries.first()
    }

    fun findByRegionCode(regionCode: String?): CountryInfo? {
        return countries.firstOrNull { it.regionCode == regionCode }
    }

    /** Validates [nationalNumber] (without the dial code) against [regionCode]'s numbering plan. */
    fun isValidNumber(regionCode: String, nationalNumber: String): Boolean {
        if (nationalNumber.isBlank()) return false
        return runCatching {
            val parsed = phoneNumberUtil.parse(nationalNumber, regionCode)
            phoneNumberUtil.isValidNumber(parsed)
        }.getOrDefault(false)
    }

    private fun buildCountries(): List<CountryInfo> {
        return phoneNumberUtil.supportedRegions
            .mapNotNull { regionCode -> regionCode.toCountryInfoOrNull() }
            .sortedBy { it.displayName }
    }

    private fun String.toCountryInfoOrNull(): CountryInfo? {
        val callingCode = phoneNumberUtil.getCountryCodeForRegion(this)
        val displayName = runCatching {
            Locale.Builder().setRegion(this).build().displayCountry
        }.getOrDefault("")
        if (callingCode <= 0 || displayName.isBlank()) return null
        return CountryInfo(
            regionCode = this,
            dialCode = "+$callingCode",
            displayName = displayName,
            flagEmoji = toFlagEmoji(),
        )
    }

    private fun String.toFlagEmoji(): String {
        return uppercase(Locale.ROOT)
            .map { letter -> String(Character.toChars(REGIONAL_INDICATOR_BASE + (letter.code - 'A'.code))) }
            .joinToString(separator = "")
    }

    private companion object {
        const val FALLBACK_REGION_CODE = "US"
        const val REGIONAL_INDICATOR_BASE = 0x1F1E6
    }
}
