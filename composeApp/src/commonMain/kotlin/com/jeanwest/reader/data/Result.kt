package com.jeanwest.reader.data


/**
 * Represents the result of an operation that can either succeed with data or fail with an error.
 *
 * This is a sealed interface with two possible outcomes:
 *  - [Success]:  Indicates a successful operation with the resulting data.
 *  - [Error]: Indicates a failed operation with the corresponding error.
 *
 *  This approach is often used as an alternative to throwing exceptions for error handling, promoting
 *  explicit handling of both success and failure scenarios.  It also facilitates functional programming
 *  patterns and improves the clarity and safety of code.
 *
 * @param D The type of data returned in case of success.
 * @param E The type of error returned in case of failure.  Must be a subclass of [Error].  Note that this is not the standard Kotlin [Error] class.
 */
sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: com.jeanwest.reader.data.Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>