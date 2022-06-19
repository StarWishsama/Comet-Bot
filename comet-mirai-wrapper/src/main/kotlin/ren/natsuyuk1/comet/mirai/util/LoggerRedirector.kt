/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.mirai.util

import mu.KLogger
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase

internal class LoggerRedirector(private val redirect: KLogger) : MiraiLoggerPlatformBase() {
    override val identity: String = redirect.name

    override fun debug0(message: String?, e: Throwable?) {
        redirect.debug(e) { message }
    }

    override fun error0(message: String?, e: Throwable?) {
        redirect.error(e) { message }
    }

    override fun info0(message: String?, e: Throwable?) {
        redirect.info(e) { message }
    }

    override fun verbose0(message: String?, e: Throwable?) {
        redirect.trace(e) { message }
    }

    override fun warning0(message: String?, e: Throwable?) {
        redirect.warn(e) { message }
    }
}
