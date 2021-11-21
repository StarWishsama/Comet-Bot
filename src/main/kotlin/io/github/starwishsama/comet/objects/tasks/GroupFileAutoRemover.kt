/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.tasks

import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.utils.CometUtil.toChain
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.isAdministrator
import java.util.regex.Pattern
import kotlin.streams.toList

object GroupFileAutoRemover {
    private val cachePool = mutableSetOf<PerGroupConfig>()

    fun execute() {
        daemonLogger.debug("正在自动删除群文件")
        val configs = GroupConfigManager.getAllConfigs()

        val groups = configs.parallelStream().filter { it.oldFileCleanFeature }.toList()

        if (groups.isEmpty()) {
            return
        }

        cachePool.addAll(groups)

        cachePool.forEach {
            handlePerGroupFile(it)
        }

        cachePool.clear()
    }

    private fun handlePerGroupFile(cfg: PerGroupConfig) {
        val group = comet.getBot().getGroup(cfg.id) ?: return

        runBlocking {
            if (!group.botAsMember.isAdministrator()) {
                group.sendMessage("机器人没有权限删除群文件, 任务已取消".toChain())
            }

            val handleTime = System.currentTimeMillis()

            val files = group.files.root.files()

            var counter = 0

            runBlocking {
                files.collect { file ->
                    val modifyTime = file.lastModifiedTime

                    if (handleTime - cfg.oldFileCleanDelay >= modifyTime) {
                        if (cfg.oldFileMatchPattern.isEmpty()) {
                            file.delete()
                            counter++
                        } else if (Pattern.matches(cfg.oldFileMatchPattern, file.name)) {
                            file.delete()
                            counter++
                        } else {
                            return@collect
                        }
                    }
                }
            }

            if (counter > 0) {
                group.sendMessage("已自动删除本群过期文件, 共 $counter 个文件.".toChain())
            }
        }
    }
}