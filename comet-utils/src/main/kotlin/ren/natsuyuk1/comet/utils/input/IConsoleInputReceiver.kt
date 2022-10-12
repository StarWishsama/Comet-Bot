package ren.natsuyuk1.comet.utils.input

import kotlin.coroutines.CoroutineContext

interface IConsoleInputReceiver {
    fun init(context: CoroutineContext)

    fun readln(): String
}
