package ren.natsuyuk1.comet.mirai.util

import kotlinx.coroutines.runBlocking

fun ClassLoader.runWith(runnable: () -> Unit) {
    val previous = Thread.currentThread().contextClassLoader

    Thread.currentThread().contextClassLoader = this

    runnable()

    Thread.currentThread().contextClassLoader = previous
}

fun ClassLoader.runWithSuspend(runnable: suspend () -> Unit) {
    runBlocking {
        val previous = Thread.currentThread().contextClassLoader

        Thread.currentThread().contextClassLoader = this@runWithSuspend

        runnable()
        Thread.currentThread().contextClassLoader = previous
    }
}
