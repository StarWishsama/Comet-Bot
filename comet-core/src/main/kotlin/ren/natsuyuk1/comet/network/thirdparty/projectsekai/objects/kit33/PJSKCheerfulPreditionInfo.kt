package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33

import kotlinx.serialization.json.*
import ren.natsuyuk1.comet.api.message.EmptyMessageWrapper
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay

data class PJSKCheerfulPreditionInfo(
    val timestamp: Long,
    val eventId: Int,
    val eventName: String,
    val eventStartAt: Long,
    val eventAggregateAt: Long,
    val theme: String,
    val teams: JsonObject,
    val names: JsonObject,
    val members: JsonObject,
    val announces: JsonObject,
    val predictRates: JsonObject,
) {
    private val teamId: Pair<Int, Int>
        get() = Pair(
            teams["0"]?.jsonPrimitive?.int ?: -1,
            teams["1"]?.jsonPrimitive?.int ?: -1
        )

    val teamName: Pair<String, String>?
        get() = run {
            val (first, second) = teamId

            Pair(
                teams[first.toString()]?.jsonPrimitive?.content ?: return null,
                teams[second.toString()]?.jsonPrimitive?.content ?: return null
            )
        }

    fun getPredictRate(): Pair<Double, Double>? {
        val (first, second) = teamId
        return Pair(
            predictRates[first.toString()]?.jsonPrimitive?.double ?: return null,
            predictRates[second.toString()]?.jsonPrimitive?.double ?: return null
        )
    }

    fun getLatestPoint(): Pair<Int, Int>? {
        val (first, second) = teamId

        val latest = announces.values.map { it as? JsonObject }.last {
            it?.get("points").let { ele ->
                ele is JsonObject && ele.values.isNotEmpty()
            }
        }?.get("points")?.jsonObject

        return Pair(
            latest?.get(first.toString())?.jsonPrimitive?.intOrNull ?: return null,
            latest?.get(second.toString())?.jsonPrimitive?.intOrNull ?: return null
        )
    }
}

fun PJSKCheerfulPreditionInfo.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        val (team1, team2) = teamName ?: return EmptyMessageWrapper
        val (t1p, t2p) = getLatestPoint() ?: return EmptyMessageWrapper
        val (p1, p2) = getPredictRate() ?: return EmptyMessageWrapper

        appendTextln("当前活动 $eventName 对战预测")
        appendLine()
        appendTextln("$team1 当前分数 $t1p")
        appendTextln("$team2 当前分数 $t2p")
        appendLine()
        appendTextln("预计胜率 >")
        appendTextln("$team1 ${(p1 * 100).fixDisplay(1)}% | $team2 ${(p2 * 100).fixDisplay(1)}%")
        appendLine()
        appendText("数据来源 33 Kit")
    }
