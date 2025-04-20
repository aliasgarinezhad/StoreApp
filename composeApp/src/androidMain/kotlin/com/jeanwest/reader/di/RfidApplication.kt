package com.jeanwest.reader.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 *  Application class for the RFID application.  This class initializes Hilt,
 *  which handles dependency injection for the application.
 *
 *  @see HiltAndroidApp
 */
@HiltAndroidApp
class RfidApplication : Application()