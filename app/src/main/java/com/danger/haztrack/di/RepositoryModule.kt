package com.danger.haztrack.di

import com.danger.haztrack.data.repository.auth.AuthRepositoryImpl
import com.danger.haztrack.data.repository.profile.UserProfileRepositoryImpl
import com.danger.haztrack.domain.repository.auth.AuthRepository
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl,
    ): UserProfileRepository
}
