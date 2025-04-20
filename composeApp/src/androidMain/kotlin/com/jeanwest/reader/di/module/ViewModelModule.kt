package com.jeanwest.reader.di.module

import androidx.compose.material3.SnackbarHostState
import com.jeanwest.reader.di.qualifier.ViewModelState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Module that provides dependencies scoped to the ViewModel.  Specifically, it handles
 * the provision of state-related dependencies for Composable UI elements within a ViewModel's
 * lifecycle.
 *
 * This module is installed in the [ViewModelComponent], ensuring that the provided dependencies
 * are available only within the scope of a ViewModel.  They will be recreated when a new
 * ViewModel is created and destroyed when the ViewModel is destroyed, preventing leaks
 * and ensuring proper resource management.
 */
@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @ViewModelState
    @Provides
    @ViewModelScoped
    fun provideSnackBar(): SnackbarHostState {
        return SnackbarHostState()
    }
}