package com.jeanwest.reader.di.module

import androidx.compose.material3.SnackbarHostState
import com.jeanwest.reader.di.qualifier.ViewModelState
import com.jeanwest.reader.view.NotificationPopupHost
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
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

    @Singleton
    @Provides
    fun provideNotificationPopupHost(): NotificationPopupHost {
        return NotificationPopupHost()
    }
}