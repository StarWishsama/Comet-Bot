package io.github.starwishsama.comet.api.thirdparty.rainbowsix

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.enums.R6Rank
import io.github.starwishsama.comet.objects.pojo.rainbowsix.R6StatusPlayer
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.IDGuidelineType
import io.github.starwishsama.comet.utils.StringUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import java.text.NumberFormat

object R6TabApi {
    private const val apiUrl = "https://r6.apitab.com/"
    private const val infoText = "=== 彩虹六号战绩查询 ===\n%s [%d级]" +
            "\n▷ 目前段位: %s current mmrchange" +
            "\n▷ KD: %s" +
            "\n▷ 爆头率: %s"
    private val num = NumberFormat.getPercentInstance()

    init {
        num.maximumIntegerDigits = 3
        num.maximumFractionDigits = 2
    }

    private fun searchPlayer(name: String): R6StatusPlayer? {
        try {
            val body: String =
                NetUtil.getPageContent("${apiUrl}search/uplay/$name?cid=${BotVariables.cfg.r6tabKey}") ?: return null
            if (CometUtil.isValidJson(body)) {
                val element: JsonElement = JsonParser.parseString(body).asJsonObject["players"]
                if (CometUtil.isValidJson(element)) {
                    val jsonObject: JsonObject = element.asJsonObject
                    val uuid: String = jsonObject.get(
                        jsonObject.keySet().iterator().next()
                    ).asJsonObject.get("profile").asJsonObject.get("p_user").asString
                    return nullableGson.fromJson(NetUtil.getPageContent("${apiUrl}player/$uuid?cid=${BotVariables.cfg.r6tabKey}")
                        ?: return null)
                }
            }
        } catch (e: Exception) {
            logger.warning("在获取 R6 玩家时出现了问题: ", e)
        }
        return null
    }

    fun getR6SInfo(player: String): String {
        try {
            if (StringUtil.isLegitId(player, IDGuidelineType.UBISOFT)) {
                val p: R6StatusPlayer? = searchPlayer(player)
                if (p != null && p.found) {
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

                    // 覆写当前段位
                    response = if (p.ranked?.asRank?.let { R6Rank.getRank(it) } != R6Rank.UNRANKED) {
                        response.replace(
                            "current".toRegex(), p.ranked?.asMMR
                                .toString() + ""
                        )
                    } else {
                        response.replace("current".toRegex(), "")
                    }

                    // 覆写 MMR 升降状态
                    val mmrChange: Int? = p.ranked?.asMMRChange
                    if (mmrChange != null && mmrChange != 0) {
                        response = if (mmrChange > 0) {
                            response.replace("mmrchange".toRegex(), "+$mmrChange")
                        } else {
                            response.replace("mmrchange".toRegex(), "" + mmrChange)
                        }
                    }
                    response = response.replace(" mmrchange".toRegex(), "")
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