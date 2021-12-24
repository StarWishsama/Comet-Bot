/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.gacha

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.gacha.impl.ArkNightInstance
import io.github.starwishsama.comet.objects.gacha.custom.CustomPool
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml.Default
import java.io.File
import java.io.IOException
import java.util.stream.Collectors

// FIXME: Refactor to unified variety type of gacha pool
object GachaService {
    val gachaPools = mutableSetOf<GachaPool>()
    private val poolPath = FileUtil.getChildFolder("gacha")

    private val yamlFilePattern = Regex(".y[a]?ml")

    fun loadGachaInstance() {
        ArkNightInstance.init()
    }

    fun loadAllPools() {
        gachaPools.clear()

        // 载入默认卡池
        if (ArkNightInstance.isUsable()) {
            gachaPools.add(ArkNightPool())
        }

        if (!poolPath.exists()) {
            poolPath.mkdirs()
            return
        }

        poolPath.listFiles()?.forEach {
            addPoolFromFile(it)
        }

        CometVariables.daemonLogger.info("成功载入了 ${gachaPools.size - 1} 个自定义卡池!")
    }

    fun addPool(gachaPool: CustomPool): GachaPool? {
        val exists = gachaPools.parallelStream().filter { it.name == gachaPool.poolName }.findAny().isPresent

        if (exists) {
            CometVariables.daemonLogger.warning("已有相同名称的卡池存在! 请检查是否忘记删除了旧文件: ${gachaPool.poolName}")
            return null
        }

        return when (gachaPool.gameType) {
            CustomPool.GameType.ARKNIGHT -> {
                ArkNightInstance.parseCustomPool(gachaPool).apply { gachaPools.add(this) }
            }
            else -> null
        }
    }

    @Suppress("unchecked_cast")
    inline fun <reified T> getPoolsByType(): List<T> {
        return gachaPools.stream().filter { it is T }.collect(Collectors.toList()) as List<T>
    }

    fun addPoolFromFile(poolFile: File) {
        require(poolFile.exists()) { "${poolFile.absolutePath} isn't exists" }

        // 不处理非 YAML 类型文件
        if (yamlFilePattern.find(poolFile.name) == null) {
            CometVariables.daemonLogger.warning("检测到不受支持的卡池文件 ${poolFile.name}")
            return
        }

        try {
            val pool = addPool(Default.decodeFromString(poolFile.getContext()))

            CometVariables.daemonLogger.info("已载入卡池 ${pool?.name}")

        } catch (e: IOException) {
            FileUtil.createErrorReportFile("解析卡池信息失败", "gacha", e, poolFile.name, e.message ?: "")
        } catch (e: SerializationException) {
            FileUtil.createErrorReportFile("解析卡池信息失败", "gacha", e, poolFile.name, e.message ?: "")
        }
    }
}