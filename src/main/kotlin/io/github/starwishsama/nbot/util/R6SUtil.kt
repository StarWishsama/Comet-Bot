package io.github.starwishsama.nbot.util

import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.enums.R6Rank
import io.github.starwishsama.nbot.objects.rainbowsix.R6Player
import java.text.NumberFormat

object R6SUtil {
    private const val infoText = "=== 彩虹六号战绩查询 ===\n%s [%d级]" +
            "\n目前段位: %s current mmrchange" +
            "\nKD: %s" +
            "\n爆头率: %s"
    private val num = NumberFormat.getPercentInstance()
    private val gson = BotConstants.gson

    private fun searchPlayer(name: String): R6Player? {
        try {
            val hr: HttpResponse =
                HttpRequest.get("https://r6.apitab.com/search/uplay/$name?cid=${BotConstants.cfg.r6tabKey}").timeout(5000).executeAsync()
            if (hr.isOk) {
                val body: String = hr.body()
                if (isValidJson(body)) {
                    val element: JsonElement =
                        JsonParser.parseString(body).asJsonObject["players"]
                    if (isValidJson(element)) {
                        val jsonObject: JsonObject = element.asJsonObject
                        val uuid: String =
                                jsonObject.get(jsonObject.keySet().iterator().next()).asJsonObject.get("profile")
                                        .asJsonObject.get("p_user").asString
                        val hr2: HttpResponse = HttpRequest.get("https://r6.apitab.com/player/$uuid?cid=${BotConstants.cfg.r6tabKey}").timeout(5000)
                                .setFollowRedirects(true).executeAsync()
                        if (hr2.isOk) {
                            return gson.fromJson(hr2.body(), R6Player::class.java)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    var response = java.lang.String.format(
                        infoText,
                        p.player?.p_name,
                        p.stats?.level,
                        p.ranked?.AS_rank?.let { R6Rank.getRank(it).rankName },
                        String.format("%.2f", p.stats?.generalpvp_kd),
                        num.format(
                            p.stats?.generalpvp_kills?.toDouble()?.let { p.stats?.generalpvp_headshot?.div(it) }
                        )
                    )
                    response = if (p.ranked?.AS_rank?.let { R6Rank.getRank(it) } !== R6Rank.UNRANKED) {
                        response.replace(
                            "current".toRegex(), p.ranked?.AS_mmr
                                .toString() + ""
                        )
                    } else {
                        response.replace("current".toRegex(), "")
                    }
                    val mmrChange: Int? = p.ranked?.AS_mmrchange
                    if (mmrChange != 0) {
                        if (mmrChange != null) {
                            response = if (mmrChange > 0) {
                                response.replace("mmrchange".toRegex(), "+$mmrChange")
                            } else {
                                response.replace("mmrchange".toRegex(), "" + mmrChange)
                            }
                        }
                    }
                    return response
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "在获取时发生了问题"
        }
        return "找不到此账号"
    }

    private fun isValidJson(json: String): Boolean {
        val jsonElement: JsonElement? = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            return false
        }
        return jsonElement?.isJsonObject ?: false
    }

    private fun isValidJson(element: JsonElement?): Boolean {
        return element?.isJsonObject ?: false
    }
}