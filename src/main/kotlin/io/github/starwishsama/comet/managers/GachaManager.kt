package io.github.starwishsama.comet.managers

import com.google.gson.JsonParseException
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.objects.gacha.custom.CustomPool
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.objects.gacha.pool.PCRPool
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.json.isUsable
import java.io.File
import kotlin.streams.toList

/**
 * [GachaManager]
 *
 * 管理所有运行时载入的卡池.
 *
 * 另见 [io.github.starwishsama.comet.objects.gacha.custom.CustomPool]
 */
object GachaManager {
    val gachaPools = mutableSetOf<GachaPool>()
    var arkNightUsable = true
    var pcrUsable = true
    private val poolPath = FileUtil.getChildFolder("gacha")

    fun loadAllPools() {
        // 载入默认卡池
        if (arkNightUsable) {
            gachaPools.add(ArkNightPool())
        }

        if (pcrUsable) {
            gachaPools.add(PCRPool())
        }

        if (!poolPath.exists()) {
            poolPath.mkdirs()
            return
        }

        if (poolPath.listFiles()?.isEmpty() == true) {
            return
        }

        poolPath.listFiles()?.forEach {
            addPoolFromFile(it)
        }

        daemonLogger.info("成功载入了 ${gachaPools.size} 个自定义卡池!")
    }

    fun addPool(gachaPool: CustomPool): Boolean {
        val exists = gachaPools.parallelStream().filter { it.name == gachaPool.poolName }.findAny().isPresent

        if (exists) {
            daemonLogger.warning("已有相同名称的卡池存在! 请检查是否忘记删除了旧文件: ${gachaPool.poolName}")
            return false
        }

        return when (gachaPool.gameType) {
            CustomPool.GameType.ARKNIGHT -> {
                parseArkNightPool(gachaPool)?.let { gachaPools.add(it) } == true
            }
            // 暂不支持 PCR 卡池自定义
            CustomPool.GameType.PCR -> {
                false
            }
        }
    }

    @Suppress("unchecked_cast")
    inline fun <reified T> getPoolsByType(): List<T> {
        return gachaPools.stream().filter { it is T }.toList() as List<T>
    }

    @Throws(JsonParseException::class)
    fun addPoolFromFile(poolFile: File) {
        require(poolFile.exists()) { "${poolFile.name} isn't exists" }

        try {
            val context = mapper.readTree(poolFile.getContext())
            require(!context.isUsable()) { "${poolFile.name} isn't a valid json file!" }
            val pool = mapper.readValue(context.traverse(), CustomPool::class.java)
            addPool(pool)
        } catch (e: Exception) {
            FileUtil.createErrorReportFile("解析卡池信息失败", "gacha", e, context.asText(), e.message ?: "")
        }
    }

    private fun parseArkNightPool(customPool: CustomPool): ArkNightPool? {
        val pool = ArkNightPool(
            customPool.poolName,
            customPool.poolDescription
        ) {
            customPool.condition.contains(obtain)
        }

        customPool.modifiedGachaItems.forEach { item ->
            val result = pool.poolItems.stream().filter { it.name == item.name }.findAny()

            result.ifPresent {
                if (item.isHidden) {
                    pool.poolItems.remove(it)
                    return@ifPresent
                }

                if (item.probability > 0) {
                    pool.highProbabilityItems[it] = item.probability
                }
            }.also {
                if (!result.isPresent) {
                    daemonLogger.warning("名为 ${item.name} 的抽卡物品不存在于游戏数据中")
                }
            }
        }
        return null
    }
}