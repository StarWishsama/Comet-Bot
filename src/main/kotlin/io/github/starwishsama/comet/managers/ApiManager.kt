package io.github.starwishsama.comet.managers

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.objects.config.api.*
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.folderIsEmpty

import java.io.File

object ApiManager {
    val apiConfigs = mutableListOf<ApiConfig>()

    inline fun <reified T> getConfig(): T {
        val result = apiConfigs.find { it is T } ?: throw ApiException("找不到指定 API 的配置")
        return result as T
    }

    fun loadAllApiConfig() {
        val apiConfigFile = FileUtil.getChildFolder("api")

        if (!apiConfigFile.exists()) {
            apiConfigFile.mkdirs()
        }

        if (apiConfigFile.folderIsEmpty()) {
            createBlankConfigs()
            daemonLogger.log(HinaLogLevel.Info, "API 配置生成成功! 注意: 自新版本开始 API 请在 /api 文件夹下配置", prefix = "API设置")
        }

        apiConfigFile.listFiles()?.forEach {
            try {
                apiConfigs.add(mapper.readValue(it))
            } catch (e: Exception) {
                daemonLogger.log(HinaLogLevel.Warn, "在处理 API 配置时出现了意外", e, prefix = "API设置")
            }
        }

        daemonLogger.log(HinaLogLevel.Info, "已加载 ${apiConfigs.size} 个配置", prefix = "API设置")
    }

    private fun createBlankConfigs() {
        val apiConfigFile = FileUtil.getChildFolder("api")
        val defaultConfigType = mutableListOf(BiliBiliConfig(), R6StatsConfig(), SauceNaoConfig(), TwitterConfig())

        defaultConfigType.forEach { configType ->
            mapper.writeValue(File(apiConfigFile, configType.apiName + ".yml").also { it.createNewFile() }, configType)
        }
    }

    fun reloadConfig() {
        apiConfigs.clear()

        loadAllApiConfig()
    }
}