package com.jeanwest.reader.useCases

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.material3.SnackbarHostState
import com.jeanwest.reader.view.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays a success beep sound using the device's ToneGenerator.
 *
 * This function creates a ToneGenerator, plays a short acknowledgment tone, and then releases the resources.
 * It handles potential exceptions that might occur during the process, such as issues with audio playback,
 * and displays an error message via a Snackbar if needed. The sound is played on a background thread
 * to avoid blocking the main thread.
 *
 * @param state The [SnackbarHostState] used to display an error message in case of failure.
 */
fun successBeep(state: SnackbarHostState) {
    CoroutineScope(Dispatchers.IO).launch {

        val beep: ToneGenerator
        try {
            beep = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            delay(300L)
            beep.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            delay(300L)
            beep.release()
        } catch (e: Exception) {
            showLog("مشکلی در پخش صدای دستگاه به وجود آمده است.", state)
        }
    }
}

/**
 * Plays an error beep sound and handles potential exceptions.
 *
 * This function plays a short error beep sound using the device's audio capabilities.  It runs on an I/O thread to avoid blocking the main thread during audio playback.  If any exception occurs during the process, it logs an error message and displays it in the provided `SnackbarHostState`.
 *
 * @param state The `SnackbarHostState` to use for displaying error messages if the beep fails.
 */
fun errorBeep(state: SnackbarHostState) {
    CoroutineScope(Dispatchers.IO).launch {
        val beep: ToneGenerator
        try {
            beep = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            delay(300L)
            beep.startTone(ToneGenerator.TONE_SUP_ERROR, 600)
            delay(900)
            beep.release()
        } catch (e: Exception) {
            showLog("مشکلی در پخش صدای دستگاه به وجود آمده است.", state)
        }
    }
}