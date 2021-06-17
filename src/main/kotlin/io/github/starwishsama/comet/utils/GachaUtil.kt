/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils

import cn.hutool.core.net.URLDecoder
import io.github.starwishsama.comet.BotVariables.arkNight
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.BotVariables.yyMMddPattern
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.GachaItem
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool
import io.github.starwishsama.comet.service.gacha.GachaConstants
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

object GachaUtil {
    /**
     * 明日方舟
     */

    const val overTimeMessage = "抽卡次数到上限了, 可以少抽一点或者等待条数自动恢复哦~\n" +
            "命令条数现在每小时会恢复100次, 封顶1000次"
    var pictureReady = false

    private const val arkNightDataApi = "https://api.github.com/repos/Kengxxiao/ArknightsGameData"
    const val arkNightData =
        "https://raw.fastgit.org/Kengxxiao/ArknightsGameData/master/zh_CN/gamedata/excel/character_table.json"

    @Suppress("UNCHECKED_CAST")
    fun combineGachaImage(gachaResult: List<GachaItem>, poolType: GachaPool): CombinedResult {
        require(gachaResult.isNotEmpty()) { "传入的抽卡结果列表不能为空!" }

        return when (poolType) {
            is ArkNightPool -> combineArkOpImage(gachaResult as List<ArkNightOperator>)
            else -> throw UnsupportedOperationException("暂不支持合成该卡池图片")
        }
    }

    /**
     * 根据抽卡结果合成图片
     */
    private fun combineArkOpImage(ops: List<ArkNightOperator>): CombinedResult {
        require(ops.isNotEmpty()) { "传入的干员列表不能为空!" }

        val lostOperators = mutableListOf<ArkNightOperator>()

        val picSize = 180

        val picHeight = 380

        val gachaResultImage = BufferedImage(picSize * ops.size, picHeight, BufferedImage.TYPE_INT_RGB)

        val graphics = gachaResultImage.createGraphics()

        var newBufferedImageWidth = 0

        for (i in ops) {
            val file = File(FileUtil.getResourceFolder().getChildFolder("ark"), i.name + ".png")

            if (!file.exists()) {
                lostOperators.add(i)
                daemonLogger.warning("明日方舟: 干员 ${i.name} 的图片不存在")
            } else {
                val inStream: InputStream = file.inputStream()

                val bufferedImage: BufferedImage = ImageIO.read(inStream)

                val imageWidth = bufferedImage.width
                val imageHeight = bufferedImage.height

                graphics.drawImage(
                    bufferedImage.getScaledInstance(
                        imageWidth,
                        imageHeight,
                        Image.SCALE_SMOOTH
                    ), newBufferedImageWidth, 0, imageWidth, imageHeight, null
                )

                newBufferedImageWidth += imageWidth

            }
        }

        graphics.dispose()

        return CombinedResult(gachaResultImage, lostOperators)

    }

    data class CombinedResult(
        val image: BufferedImage,
        val lostItem: List<GachaItem>
    )

    fun getStarText(rare: Int): String = buildString {
        for (i in 0 until rare) {
            append("★")
        }
    }

    fun checkHasGachaTime(user: BotUser, time: Int): Boolean =
        (user.commandTime >= time || user.compareLevel(UserLevel.ADMIN)) && time <= 10000

    @Suppress("HttpUrlsUsage")
    fun downloadArkNightImage() {
        val arkLoc = FileUtil.getResourceFolder().getChildFolder("ark")

        val ele = Jsoup.connect(
            "http://prts.wiki/w/PRTS:%E6%96%87%E4%BB%B6%E4%B8%80%E8%A7%88/%E5%B9%B2%E5%91%98%E7%B2%BE%E8%8B%B10%E5%8D%8A%E8%BA%AB%E5%83%8F"
        ).timeout(5_000).get().getElementsByClass("mw-parser-output")[0].select("a")

        /**
         * PRTS 实际保有干员半身立绘量
         */
        val actualCount = ele.size

        if (arkLoc.filesCount() == 0) {
            val startTime = LocalDateTime.now()
            daemonLogger.info("正在下载 明日方舟图片资源文件")

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
                    daemonLogger.warning("获取图片 http://prts.wiki/${it.attr("href")} 失败, 请手动下载.")
                }
            }

            // http://prts.wiki/images/f/ff/半身像_诗怀雅_1.png

            downloadList.forEach { url ->
                val opName = URLDecoder.decode(url.split("/")[4].split("_")[1], Charsets.UTF_8)

                if (arkNight.stream().filter { it.name == opName }.findFirst().isPresent) {
                    try {
                        val file = File(arkLoc, url)
                        if (!file.exists()) {
                            val result = TaskUtil.executeRetry(3) {
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
                            daemonLogger.warning("下载 $url 时出现了意外", e)
                        else
                            daemonLogger.warning("下载异常: ${e.message ?: "无信息"}")
                        return@forEach
                    } finally {
                        if (successCount > 0 && successCount % 10 == 0) {
                            daemonLogger.info("明日方舟 > 已下载 $successCount/${arkNight.size}")
                        }
                    }
                }
            }

            daemonLogger.info("明日方舟 > 缺失资源文件下载完成 [$successCount/${arkNight.size}], 耗时 ${startTime.getLastingTimeAsString()}")
        }

        pictureReady = true
    }

    fun arkNightDataCheck(location: File) {
        var exists = true

        daemonLogger.info("明日方舟 > 检查是否为旧版本数据...")
        if (!location.exists()) {
            daemonLogger.info("明日方舟 > 你还没有卡池数据, 正在自动下载新数据")
            exists = false
        }

        val result = mapper.readTree(NetUtil.executeHttpRequest(arkNightDataApi).body?.string())
        val updateTime = LocalDateTime.parse(result["updated_at"].asText(), DateTimeFormatter.ISO_DATE_TIME)

        if (!exists) {
            val cache = NetUtil.downloadFile(FileUtil.getCacheFolder(), arkNightData, location.name)
            cache.deleteOnExit()
            Files.copy(cache.toPath(), location.toPath(), StandardCopyOption.REPLACE_EXISTING)
            daemonLogger.info("成功下载明日方舟卡池数据!")
        } else if (location.exists() && location.lastModified().toLocalDateTime() < updateTime) {
            // 这个检测并不准确, 因为其他服务器数据更新的时候也算, 而 Github API 似乎看不到最新 commit (?)
            daemonLogger.info("明日方舟干员数据更新了 (在 ${yyMMddPattern.format(updateTime)}), 请自行更新")
        } else {
            daemonLogger.info("明日方舟干员数据为最新版本: ${yyMMddPattern.format(updateTime)}")
        }
    }

    fun arkPictureIsUsable(): Boolean = cfg.arkDrawUseImage && pictureReady

    fun hasOperator(name: String): Boolean = GachaConstants.arkNightDefault.contains(name)
}