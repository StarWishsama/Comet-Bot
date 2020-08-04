package io.github.starwishsama.comet.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.enums.R6Rank
import io.github.starwishsama.comet.objects.pojo.rainbowsix.R6Player
import java.text.NumberFormat

object R6SUtil {
    private const val infoText = "=== 彩虹六号战绩查询 ===\n%s [%d级]" +
            "\n目前段位: %s current mmrchange" +
            "\nKD: %s" +
            "\n爆头率: %s"
    private val num = NumberFormat.getPercentInstance()
    private val gson = BotVariables.gson

    private fun searchPlayer(name: String): R6Player? {
        try {
            val body: String =
                NetUtil.getPageContent("https://r6.apitab.com/search/uplay/$name?cid=${BotVariables.cfg.r6tabKey}")
            if (BotUtil.isValidJson(body)) {
                val element: JsonElement = JsonParser.parseString(body).asJsonObject["players"]
                if (BotUtil.isValidJson(element)) {
                    val jsonObject: JsonObject = element.asJsonObject
                    val uuid: String = jsonObject.get(jsonObject.keySet().iterator().next()).asJsonObject.get("profile").asJsonObject.get("p_user").asString
                    return gson.fromJson(
                        NetUtil.getPageContent("https://r6.apitab.com/player/$uuid?cid=${BotVariables.cfg.r6tabKey}"),
                        R6Player::class.java
                    )
                }
            }
        } catch (e: Exception) {
            logger.warning("在获取 R6 玩家时出现了问题: ", e)
        }
        return null
    }

    fun getR6SInfo(player: String): String {
        try {
            if (BotUtil.isLegitId(player)) {
                val p: R6Player? = searchPlayer(player)
                if (p != null && p.found) {
                    num.maximumIntegerDigits = 3
                    num.maximumFractionDigits = 2
                    var response = String.format(
                            infoText,
                            p.player?.playerName,
                            p.stats?.level,
                            p.ranked?.asRank?.let { R6Rank.getRank(it).rankName },
                            p.stats?.generalKd,
                            num.format(
                                    p.stats?.generalKills?.toDouble()?.let { p.stats?.generalHeadShot?.div(it) }
                            )
                    )
                    response = if (p.ranked?.asRank?.let { R6Rank.getRank(it) } != R6Rank.UNRANKED) {
                        response.replace(
                                "current".toRegex(), p.ranked?.asMMR
                                .toString() + ""
                        )
                    } else {
                        response.replace("current".toRegex(), "")
                    }
                    val mmrChange: Int? = p.ranked?.asMMRChange
                    if (mmrChange != null && mmrChange != 0) {
                        response = if (mmrChange > 0) {
                            response.replace("mmrchange".toRegex(), "+$mmrChange")
                        } else {
                            response.replace("mmrchange".toRegex(), "" + mmrChange)
                        }
                    }
                    return response
                }
            }
        } catch (e: Exception) {
            logger.warning("[R6] 在获取 R6 玩家信息时出现错误", e)
            return "在获取时发生了问题"
        }
        return "找不到此账号"
    }
}