/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.i18n

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getChildFolder
import io.github.starwishsama.comet.utils.getContext
import net.mamoe.yamlkt.Yaml.Default
import java.io.File

class LocalizationManager {
    private val currentLanguage: Language = Language.ZH_CN
    private val localizationYaml: Map<String?, Any?>

    init {
        val localizedFolder = FileUtil.getResourceFolder().getChildFolder("i18n")

        val localizationFile = File(localizedFolder, currentLanguage.fileName)

        if (localizationFile.exists()) {
            localizationYaml = Default.decodeMapFromString(localizationFile.getContext())
            daemonLogger.info("多语言服务已启动! 使用语言: $currentLanguage")
        } else {
            localizationYaml = mutableMapOf()
            daemonLogger.warning("多语言文件未被正确生成! 部分文本可能会受到影响")
        }
    }

    fun getLocalizationText(target: String): String {
        if (localizationYaml.isEmpty()) {
            return "$target - 占位符"
        }

        try {
            val nodes = target.split(".")
            if (nodes.size == 1) {
                return localizationYaml[nodes[0]]?.toString() ?: "$target - 占位符"
            } else {
                var currentNode: Map<String?, String?> = mutableMapOf()
                nodes.forEach {
                    val current = localizationYaml[it]

                    if (nodes.last() != it) {
                        currentNode = getDeepNode(current) ?: return "$target - 占位符"
                    } else {
                        return currentNode[it] ?: "$target - 占位符"
                    }
                }
            }
        } catch (e: Exception) {
            daemonLogger.warning("获取本地化文本失败, 可能是文件损坏?", e)
            return "$target - 占位符"
        }

        return "$target - 占位符"
    }

    @Suppress("UNCHECKED_CAST")
    internal fun getDeepNode(deepNode: Any?): Map<String?, String?>? {
        if (deepNode != null && deepNode::class.java == LinkedHashMap::class.java) {
            return deepNode as Map<String?, String?>
        }

        return null
    }
}

enum class Language(val fileName: String) {
    ZH_CN("zh_cn.yml")
}