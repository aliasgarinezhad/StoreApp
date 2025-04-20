package com.jeanwest.reader.useCases

import io.sentry.Sentry

/**
 * A custom [Thread.UncaughtExceptionHandler] that intercepts uncaught exceptions,
 * reports them to Sentry, and then delegates to a default handler.
 *
 * This allows for centralized error reporting even when exceptions are not explicitly
 * caught and handled within individual threads.
 *
 * @property default The default [Thread.UncaughtExceptionHandler] to delegate to after
 *                   reporting the exception to Sentry.  This is typically the system's
 *                   default handler.
 */
class ExceptionHandler(var default: Thread.UncaughtExceptionHandler) :
    Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {

        Sentry.captureException(exception)
        default.uncaughtException(thread, exception)
    }
}