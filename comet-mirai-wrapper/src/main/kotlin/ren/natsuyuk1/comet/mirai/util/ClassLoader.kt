package ren.natsuyuk1.comet.mirai.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun <T> ClassLoader.runWith(runnable: () -> T): T {
    Thread.currentThread().contextClassLoader = this
    return runnable()
}

fun <T> ClassLoader.runWithSuspend(runnable: suspend () -> T): T = runBlocking {
    Thread.currentThread().contextClassLoader = this@runWithSuspend
    return@runBlocking runnable()
}

fun <T> ClassLoader.runWithScope(scope: CoroutineScope, runnable: suspend () -> T) = scope.launch {
    Thread.currentThread().contextClassLoader = this@runWithScope
    runnable()
}
