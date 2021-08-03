/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.parseAsClass
import io.github.starwishsama.comet.utils.writeClassToJson
import java.io.File

/**
 * [CommandPropsManager]
 *
 * 管理命令配置
 *
 */
object CommandPropsManager {
    private val path = FileUtil.getChildFolder("commands")

    fun load() {
        if (!path.exists()) {
            path.mkdirs()

            CommandManager.getCommands().forEach { cmd ->
                val config = File(path, "${cmd.name}.json").also { if (!it.exists()) it.createNewFile() }

                config.writeClassToJson(cmd.props)
            }
        } else {
            path.listFiles()?.forEach {
                val fileName = it.nameWithoutExtension
                val command = CommandManager.getCommand(fileName)

                if (command != null) {
                    try {
                        command.props = it.parseAsClass()
                    } catch (e: RuntimeException) {
                        daemonLogger.warning("在解析命令 [${command.props.name}] 配置文件时遇到问题, 使用默认配置", e)
                    }
                }
            }
        }
    }

    fun save() {
        CommandManager.getCommands().forEach { cmd ->
            val config = File(path, "${cmd.name}.json").also { if (!it.exists()) it.createNewFile() }

            config.writeClassToJson(cmd.props)
        }
    }
}