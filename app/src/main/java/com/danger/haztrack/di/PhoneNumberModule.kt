package com.danger.haztrack.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PhoneNumberModule {
    @Provides
    @Singleton
    fun providePhoneNumberUtil(@ApplicationContext context: Context): PhoneNumberUtil {
        // Unlike the upstream Google library, this Android port has no getInstance() singleton
        // and loads its metadata from assets, so it must be created once and reused (creating a
        // new instance is expensive) — see io.michaelrocks:libphonenumber-android's README.
        return PhoneNumberUtil.createInstance(context)
    }
}
