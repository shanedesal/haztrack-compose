package com.danger.haztrack.di

import com.danger.haztrack.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth) {
                // The only browser-redirect flow this app uses is the recovery link — Google
                // sign-in is native ID-token, bypassing this. PKCE's code-verifier step exists
                // to survive the redirect landing in a *different* browser than the one that
                // started the flow; that doesn't apply to a link tapped cold from email, so
                // Implicit keeps this self-contained in one redirect.
                flowType = FlowType.IMPLICIT
                scheme = "com.danger.haztrack"
                host = "reset-password"
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuth(supabaseClient: SupabaseClient): Auth = supabaseClient.auth
}
