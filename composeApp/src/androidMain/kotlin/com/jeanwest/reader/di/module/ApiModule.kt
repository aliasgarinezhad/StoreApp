package com.jeanwest.reader.di.module

import androidx.compose.material3.SnackbarHostState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *  Dagger Hilt module for providing application-wide dependencies related to API interaction
 *  and UI feedback (e.g., Snackbar handling).  This module is installed in the [SingletonComponent],
 *  making its provided dependencies available throughout the application's lifecycle as singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Singleton
    @Provides
    fun state(): SnackbarHostState {
        return SnackbarHostState()
    }
}