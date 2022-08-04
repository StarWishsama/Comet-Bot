package ren.natsuyuk1.comet.mirai.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun <T> ClassLoader.runWith(runnable: () -> T): T {
    val previous = Thread.currentThread().contextClassLoader

    Thread.currentThread().contextClassLoader = this

    try {
        return runnable()
    } finally {
        Thread.currentThread().contextClassLoader = previous
    }
}

fun <T> ClassLoader.runWithSuspend(runnable: suspend () -> T): T = runBlocking {
    val previous = Thread.currentThread().contextClassLoader

    Thread.currentThread().contextClassLoader = this@runWithSuspend

    try {
        return@runBlocking runnable()
    } finally {
        Thread.currentThread().contextClassLoader = previous
    }
}

fun <T> ClassLoader.runWithScope(scope: CoroutineScope, runnable: suspend () -> T) = scope.launch {
    val previous = Thread.currentThread().contextClassLoader

    Thread.currentThread().contextClassLoader = this@runWithScope

    try {
        runnable()
    } finally {
        Thread.currentThread().contextClassLoader = previous
    }
}
