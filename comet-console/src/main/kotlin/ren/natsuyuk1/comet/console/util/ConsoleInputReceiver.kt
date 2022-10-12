package ren.natsuyuk1.comet.console.util

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.isActive
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.input.IConsoleInputReceiver
import kotlin.coroutines.CoroutineContext

object ConsoleInputReceiver : IConsoleInputReceiver {
    private var scope = ModuleScope("comet-console-input-receiver")
    private var _input = atomic("")
    private var input by _input

    override fun init(context: CoroutineContext) {
        scope = ModuleScope("comet-console-input-receiver", context)

        while (scope.isActive) {
            _input.update { Console.readln() }
        }
    }

    override fun readln(): String = input
}
