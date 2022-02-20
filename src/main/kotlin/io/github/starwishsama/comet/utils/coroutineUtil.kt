package io.github.starwishsama.comet.utils

import kotlinx.coroutines.CancellationException

fun <T> Result<T>.noCatchCancellation(action: (exception: Throwable) -> Unit): Result<T> {
    return onFailure {
        if (it is CancellationException) throw it
        action(it)
    }
}