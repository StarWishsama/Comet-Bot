package io.github.starwishsama.comet.i18n

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getChildFolder
import io.github.starwishsama.comet.utils.getContext
import net.mamoe.yamlkt.Yaml
import java.io.File

class LocalizationManager {
    private val currentLanguage: Language = Language.ZH_CN
    private var localizationYaml: Map<String?, Any?> = mutableMapOf()

    init {
        val localizedFolder = FileUtil.getResourceFolder().getChildFolder("i18n")

        val localizationFile = File(localizedFolder, currentLanguage.fileName)
        if (localizationFile.exists()) {
            localizationYaml = Yaml.Default.decodeMapFromString(localizationFile.getContext())
            daemonLogger.info("多语言服务已启动! 使用语言: $currentLanguage")
        } else {
            daemonLogger.warning("多语言文件未被正确生成! 部分文本可能会受到影响")
        }
    }

    fun getLocalizationText(target: String): String {
        if (localizationYaml.isEmpty()) {
            return "占位符"
        }

        try {
            val nodes = target.split(".")
            if (nodes.size == 1) {
                return localizationYaml[nodes[0]].toString()
            } else {
                var currentNode: Map<String, String> = mutableMapOf()
                nodes.forEach {
                    val current = localizationYaml[it]

                    if (nodes.last() != it) {
                        currentNode = getDeepNode(current) ?: return "占位符"
                    } else {
                        return currentNode[it] ?: "占位符"
                    }
                }
            }
        } catch (e: Exception) {
            daemonLogger.warning("获取本地化文本失败, 可能是文件损坏?", e)
            return "占位符"
        }

        return "占位符"
    }

    @Suppress("UNCHECKED_CAST")
    internal fun getDeepNode(deepNode: Any?): Map<String, String>? {
        if (deepNode != null && deepNode::class.java == LinkedHashMap::class.java) {
            return deepNode as Map<String, String>
        }

        return null
    }
}

enum class Language(val fileName: String) {
    ZH_CN("zh_cn.yml")
}