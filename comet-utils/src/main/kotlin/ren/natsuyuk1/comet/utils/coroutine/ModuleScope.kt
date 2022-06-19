/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.utils.coroutine

import kotlinx.coroutines.*
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

/**
 * Provide a common [CoroutineScope] for module
 * with common [CoroutineExceptionHandler] and [Dispatchers.Default]
 *
 * In general, you should add a [ModuleScope] as a class member field
 * or object member field with some `init(parentCoroutineContext)` method.
 * And launch or dispatch jobs coroutines by [ModuleScope]
 *
 * @property parentJob specified [Job] with parent coroutine context [Job]
 *
 * @param moduleName coroutine name, and name also would appear
 * @param parentContext parent scope [CoroutineContext]
 * @param dispatcher custom [CoroutineDispatcher]
 * @param exceptionHandler custom expcetion handler lambda
 * with [CoroutineContext], [Throwable], [KLogger] the specified logger, [String] module name
 */
open class ModuleScope(
    private val moduleName: String = "UnnamedModule",
    parentContext: CoroutineContext = EmptyCoroutineContext,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    exceptionHandler: (CoroutineContext, Throwable, KLogger, String) -> Unit =
        { _, e, _, _ -> logger.error(e) { "Caught Exception on $moduleName" } }
) : CoroutineScope {

    private val parentJob = SupervisorJob(parentContext[Job])

    override val coroutineContext: CoroutineContext =
        parentContext + parentJob + CoroutineName(moduleName) + dispatcher +
            CoroutineExceptionHandler { context, e ->
                exceptionHandler(context, e, logger, moduleName)
            }

    fun dispose() {
        parentJob.cancel()
        onClosed()
    }

    open fun onClosed() {
    }

    fun name() = moduleName
}
