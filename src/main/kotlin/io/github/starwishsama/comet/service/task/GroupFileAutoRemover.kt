/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.task

import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.utils.CometUtil.toChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.isAdministrator
import java.util.regex.Pattern

object GroupFileAutoRemover {
    private val cachePool = mutableSetOf<PerGroupConfig>()

    fun execute() {
        val configs = GroupConfigManager.getAllConfigs()

        configs.forEach {
            if (!it.oldFileCleanFeature) {
                return@forEach
            } else {
                cachePool.add(it)
            }
        }

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

            val files = group.filesRoot.listFilesCollection()

            for (file in files) {
                if (file.isDirectory()) {
                    continue
                }

                val modifyTime = file.getInfo()?.lastModifyTime ?: continue

                if (handleTime - cfg.oldFileCleanDelay >= modifyTime) {
                    if (cfg.oldFileMatchPattern.isEmpty()) {
                        file.delete()
                    } else if (Pattern.matches(cfg.oldFileMatchPattern, file.name)) {
                        file.delete()
                    } else {
                        continue
                    }
                }
            }

            group.sendMessage("已自动删除本群过期文件.".toChain())
        }
    }
}