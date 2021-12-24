/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.gacha.impl

import cn.hutool.core.net.URLDecoder
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.gacha.GachaInstance
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.file.ArkNightData
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.custom.CustomPool
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.service.gacha.GachaConstants
import io.github.starwishsama.comet.utils.*
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.math.MathUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ArkNightInstance : GachaInstance("明日方舟") {
    var usable: Boolean = false
        private set
    var isDownloading = false
        private set
    var pictureReady = false
        private set

    private const val arkNightDataApi = "https://api.github.com/repos/Kengxxiao/ArknightsGameData"
    private const val dataURL =
        "https://raw.fastgit.org/Kengxxiao/ArknightsGameData/master/zh_CN/gamedata/excel/character_table.json"

    /** 明日方舟卡池数据 */
    private val arkNight: MutableList<ArkNightOperator> = mutableListOf()

    override fun init() {
        loadGachaData()

        if (isUsable()) {
            loadDefaultArkNightData()
        }
    }

    override fun isUsable(): Boolean = usable

    override fun loadGachaData() {
        val dataLocation = ArkNightData.file

        if (!dataLocation.exists()) {
            CometVariables.daemonLogger.info("未检测到明日方舟游戏数据, 抽卡模拟器将无法使用")
            usable = false
        }

        CometVariables.mapper.readTree(dataLocation).elements().forEach { t ->
            arkNight.add(CometVariables.mapper.readValue(t.traverse()))
        }

        CometVariables.daemonLogger.info("成功载入明日方舟游戏数据, 共 ${arkNight.size} 个干员")
        if (CometVariables.cfg.arkDrawUseImage) {
            if (System.getProperty("java.awt.headless") != "true" && RuntimeUtil.getOsName().lowercase()
                    .contains("linux")
            ) {
                CometVariables.daemonLogger.info("检测到 Linux 系统, 正在启用无头模式")
                System.setProperty("java.awt.headless", "true")
            }

            TaskUtil.dispatcher.run {
                checkArkNightImage()
            }
        }
    }

    override fun checkUpdate(): Boolean {
        val result = CometVariables.mapper.readTree(NetUtil.executeHttpRequest(arkNightDataApi).body?.string())
        val updateTime = LocalDateTime.parse(result["updated_at"].asText(), DateTimeFormatter.ISO_DATE_TIME)
        val location = ArkNightData.file

        return location.exists() && location.lastModified().toLocalDateTime() < updateTime
    }

    override fun downloadFile() {
        val location = ArkNightData.file
        isDownloading = true

        try {
            val exists = location.exists()

            CometVariables.daemonLogger.info("明日方舟 > 检查是否为旧版本数据...")

            val needUpdate = checkUpdate()

            if (!exists) {
                CometVariables.daemonLogger.info("明日方舟 > 你还没有卡池数据, 正在自动下载新数据")
                val cache = NetUtil.downloadFile(FileUtil.getCacheFolder(), dataURL, location.name)
                cache.deleteOnExit()
                Files.copy(cache.toPath(), location.toPath(), StandardCopyOption.REPLACE_EXISTING)
                CometVariables.daemonLogger.info("成功下载明日方舟卡池数据!")
            } else if (needUpdate) {
                // 这个检测并不准确, 因为其他服务器数据更新的时候也算, 而 Github API 似乎看不到最新 commit (?)
                CometVariables.daemonLogger.info("明日方舟干员数据更新了, 正在下载...")
                TaskUtil.dispatcher.runCatching {
                    val cache = NetUtil.downloadFile(FileUtil.getCacheFolder(), dataURL, location.name)
                    cache.deleteOnExit()
                    Files.copy(cache.toPath(), location.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }.onSuccess {
                    CometVariables.daemonLogger.info("成功下载明日方舟干员数据!")
                }.onFailure {
                    CometVariables.daemonLogger.error("明日方舟干员数据下载失败, 将使用缓存数据", it)
                }
            } else {
                CometVariables.daemonLogger.info("明日方舟干员数据为最新版本")
            }
        } catch (e: IOException) {
            CometVariables.daemonLogger.warning("解析明日方舟游戏数据失败, ${e.message}\n注意: 数据来源于 Github, 国内用户无法下载请自行下载替换\n替换位置: ./res/arkNights.json\n链接: $dataURL")
        } finally {
            isDownloading = false
        }
    }

    override fun parseGachaResult(user: CometUser, result: GachaResult): MessageWrapper {
        TODO("Not yet implemented")
    }

    override fun parseCustomPool(customPool: CustomPool): GachaPool {
        val pool = ArkNightPool(
            customPool.poolName,
            customPool.displayPoolName,
            customPool.poolDescription
        ) {
            (GachaUtil.hasOperator(this.name) || customPool.modifiedGachaItems.stream().filter { it.name == this.name }
                .findAny().isPresent) &&
                    (if (customPool.condition.isNotEmpty()) !customPool.condition.contains(obtain) else true)
        }

        customPool.modifiedGachaItems.forEach { item ->
            val result = pool.poolItems.stream().filter { it.name == item.name }.findAny()

            result.ifPresent {
                if (item.isHidden) {
                    pool.poolItems.remove(it)
                    return@ifPresent
                }

                if (item.probability > 0) {
                    if (item.weight <= 1) {
                        pool.highProbabilityItems[it] = item.probability
                    } else {
                        pool.highProbabilityItems[it] = MathUtil.calculateWeight(
                            pool.poolItems.size,
                            pool.poolItems.filter { poolItem -> poolItem.rare == result.get().rare }.size,
                            item.weight
                        )
                    }
                }
            }.also {
                if (!result.isPresent) {
                    CometVariables.daemonLogger.warning("名为 ${item.name} 的抽卡物品不存在于游戏数据中")
                }
            }
        }

        return pool
    }

    private fun loadDefaultArkNightData() {
        val default = File(FileUtil.getResourceFolder(), "default_arknight.json")

        if (!default.exists()) {
            CometVariables.daemonLogger.warning("无法加载默认明日方舟数据: ${default.name} 不存在")
            return
        }

        val node = CometVariables.mapper.readTree(default)

        node.forEach {
            it.forEach { inside ->
                GachaConstants.arkNightDefault.add(inside.asText())
            }
        }

        CometVariables.daemonLogger.info("加载默认明日方舟数据成功, 共 ${GachaConstants.arkNightDefault.size} 个干员")
    }

    @Suppress("HttpUrlsUsage")
    fun checkArkNightImage() {
        val arkLoc = FileUtil.getResourceFolder().getChildFolder("ark")

        if (arkLoc.filesCount() == 0) {
            val startTime = LocalDateTime.now()
            CometVariables.daemonLogger.info("正在下载 明日方舟图片资源文件")

            val ele = Jsoup.connect(
                "http://prts.wiki/w/PRTS:%E6%96%87%E4%BB%B6%E4%B8%80%E8%A7%88/%E5%B9%B2%E5%91%98%E7%B2%BE%E8%8B%B10%E5%8D%8A%E8%BA%AB%E5%83%8F"
            ).timeout(5_000).get().getElementsByClass("mw-parser-output")[0].select("a")

            var successCount = 0

            val downloadList = mutableSetOf<String>()

            ele.forEach {
                try {
                    val image = Jsoup.connect("http://prts.wiki/" + it.attr("href")).timeout(10_000).get()
                    downloadList.plusAssign(image.getElementsByClass("fullImageLink")[0].select("a").attr("href"))

                    // 休息 1.5 秒, 避免给 PRTS 服务器带来太大压力
                    runBlocking {
                        delay(1_500)
                    }
                } catch (e: Exception) {
                    CometVariables.daemonLogger.warning("获取图片 http://prts.wiki/${it.attr("href")} 失败, 请手动下载.")
                }
            }

            // http://prts.wiki/images/f/ff/半身像_诗怀雅_1.png

            downloadList.forEach { url ->
                val opName = URLDecoder.decode(url.split("/")[4].split("_")[1], Charsets.UTF_8)

                if (arkNight.stream().filter { it.name == opName }.findFirst().isPresent) {
                    try {
                        val file = File(arkLoc, url)
                        if (!file.exists()) {
                            val result = TaskUtil.executeWithRetry(3) {
                                NetUtil.downloadFile(arkLoc, "http://prts.wiki$url", "$opName.png")
                            }
                            if (result != null) throw result
                            successCount++

                            // 休息三秒钟, 避免给 PRTS 服务器带来太大压力
                            runBlocking {
                                delay(3_000)
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is ApiException)
                            CometVariables.daemonLogger.warning("下载 $url 时出现了意外", e)
                        else
                            CometVariables.daemonLogger.warning("下载异常: ${e.message ?: "无信息"}")
                        return@forEach
                    } finally {
                        if (successCount > 0 && successCount % 10 == 0) {
                            CometVariables.daemonLogger.info("明日方舟 > 已下载 $successCount/${arkNight.size}")
                        }
                    }
                }
            }

            CometVariables.daemonLogger.info("明日方舟 > 缺失资源文件下载完成 [$successCount/${arkNight.size}], 耗时 ${startTime.getLastingTimeAsString()}")
        }

        pictureReady = true
    }

    fun getArkNightOperators(): List<ArkNightOperator> {
        return Collections.unmodifiableList(arkNight)
    }
}