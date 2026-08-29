package com.danger.haztrack.presentation.auth.common

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.danger.haztrack.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Thin wrapper around Credential Manager used to obtain a Google ID token for
 * [com.danger.haztrack.domain.usecase.auth.SignInWithGoogleUseCase].
 *
 * This lives in the presentation layer (not injected via Hilt) because
 * [CredentialManager.getCredential] needs an Activity [Context] to display its UI, and
 * ViewModels must stay free of Android UI Context references.
 */
class GoogleAuthClient {

    suspend fun requestIdToken(context: Context): String {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(context = context, request = request)
        val credential = response.credential

        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) { "Unexpected credential type returned for Google sign-in" }

        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    suspend fun signOut(context: Context) {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
}
