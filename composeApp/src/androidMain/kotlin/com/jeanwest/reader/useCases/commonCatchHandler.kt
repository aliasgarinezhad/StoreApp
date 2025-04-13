package com.jeanwest.reader.useCases

import io.sentry.Sentry
import io.sentry.SentryLevel

/**
 * A common exception handler for catching and logging exceptions.
 *
 * This function captures the stack trace of the provided exception and sends it as an informational message to Sentry.
 * It serves as a centralized place to handle unexpected exceptions, allowing for consistent error reporting.
 *
 * @param exception The exception that needs to be handled and logged.  Its stack trace will be captured and sent to Sentry.
 */
fun commonCatchHandler(exception: Exception) {
    Sentry.captureMessage(exception.stackTraceToString(), SentryLevel.INFO)
}