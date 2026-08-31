package com.danger.haztrack.domain.usecase.auth

object AuthInputValidation {
    private val emailPattern = Regex(
        pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        option = RegexOption.IGNORE_CASE,
    )

    fun email(value: String): String {
        val email = value.trim()
        require(emailPattern.matches(email)) {
            "A valid email address is required"
        }
        return email
    }

    fun password(value: String): String {
        require(value.length >= MIN_PASSWORD_LENGTH) {
            "Password must contain at least $MIN_PASSWORD_LENGTH characters"
        }
        return value
    }

    fun name(value: String): String {
        val name = value.trim()
        require(name.isNotBlank()) {
            "Name must not be blank"
        }
        return name
    }

    fun googleIdToken(value: String): String {
        require(value.isNotBlank()) {
            "Google ID token must not be blank"
        }
        return value
    }

    private const val MIN_PASSWORD_LENGTH = 6
}
