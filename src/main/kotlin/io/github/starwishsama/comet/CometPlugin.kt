/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.listeners.mirai.BotLoginListener
import io.github.starwishsama.comet.startup.CometRuntime
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.SemVersion

object CometPlugin : KotlinPlugin(
    JvmPluginDescription(
        "io.github.starwishsama.comet",
        SemVersion.invoke(BuildConfig.version),
        "Comet"
    )
) {
    override fun onEnable() {
        BotLoginListener.listen()

        try {
            CometRuntime.postSetup()
        } catch (e: Exception) {
            daemonLogger.serve("无法正常加载 Comet, 插件将自动关闭", e)
            disable()
        }
    }

    override fun onDisable() {
        CometRuntime.shutdownTask()
    }
}
