/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.managers

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.objects.config.api.*
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.filesCount
import io.github.starwishsama.comet.utils.folderIsEmpty

import java.io.File

object ApiManager {
    private val defaultConfigType =
        mutableListOf(BiliBiliConfig(), R6StatsConfig(), SauceNaoConfig(), TwitterConfig(), ThirdPartyMusicConfig())
    val apiConfigs = mutableSetOf<ApiConfig>()

    inline fun <reified T> getConfig(): T {
        val result = apiConfigs.find { it is T } ?: throw ApiException("找不到指定 API 的配置")
        return result as T
    }

    fun loadAllApiConfig() {
        val apiConfigFile = FileUtil.getChildFolder("api")

        if (!apiConfigFile.exists()) {
            apiConfigFile.mkdirs()
        }

        if (apiConfigFile.folderIsEmpty() || apiConfigFile.filesCount() < defaultConfigType.size) {
            createDefaultConfigs()
            daemonLogger.log(HinaLogLevel.Info, "API 配置生成成功! 注意: 自新版本开始 API 配置请在 /api 文件夹下配置", prefix = "API设置")
        }

        apiConfigFile.listFiles()?.forEach {
            try {
                addConfig(mapper.readValue(it))
            } catch (e: Exception) {
                daemonLogger.log(HinaLogLevel.Warn, "在处理 API 配置时出现了意外", e, prefix = "API设置")
            }
        }

        daemonLogger.log(HinaLogLevel.Info, "已加载 ${apiConfigs.size} 个配置", prefix = "API设置")
    }

    fun addConfig(config: ApiConfig) {
        apiConfigs.add(config)
    }

    private fun createDefaultConfigs() {
        val apiConfigFile = FileUtil.getChildFolder("api")

        defaultConfigType.forEach { configType ->
            val target = File(apiConfigFile, configType.apiName + ".yml")
            if (!target.exists()) {
                mapper.writeValue(target.also { it.createNewFile() }, configType)
            }
        }
    }

    fun reloadConfig() {
        apiConfigs.clear()
        loadAllApiConfig()
    }
}