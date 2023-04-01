package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay

@Serializable
data class PJSKCheerfulPreditionInfo(
    val timestamp: Long,
    val eventId: Int,
    val eventName: String,
    val eventStartAt: Long,
    val eventAggregateAt: Long,
    val theme: String,
    val teams: JsonArray,
    val names: JsonObject,
    val members: JsonObject,
    val announces: JsonArray,
    val predictRates: JsonObject,
) {
    private val teamId: Pair<Int, Int>
        get() = Pair(
            teams[0].jsonPrimitive.int,
            teams[1].jsonPrimitive.int,
        )

    val teamName: Pair<String, String>?
        get() = run {
            val (first, second) = teamId

            Pair(
                names[first.toString()]?.jsonPrimitive?.content ?: return null,
                names[second.toString()]?.jsonPrimitive?.content ?: return null,
            )
        }

    fun getPredictRate(): Pair<Double, Double>? {
        val (first, second) = teamId
        return Pair(
            predictRates[first.toString()]?.jsonPrimitive?.double ?: return null,
            predictRates[second.toString()]?.jsonPrimitive?.double ?: return null,
        )
    }

    fun getLatestAnnouncement(): Pair<Int, Int>? {
        val (first, second) = teamId

        val latest = announces.last {
            it.jsonObject["points"] is JsonObject
        }.jsonObject["points"]?.jsonObject

        return Pair(
            latest?.get(first.toString())?.jsonPrimitive?.intOrNull ?: return null,
            latest[second.toString()]?.jsonPrimitive?.intOrNull ?: return null,
        )
    }

    fun getMemberCount(): Pair<Int, Int>? {
        val (first, second) = teamId

        return Pair(
            members[first.toString()]?.jsonPrimitive?.intOrNull ?: return null,
            members[second.toString()]?.jsonPrimitive?.intOrNull ?: return null,
        )
    }
}

fun PJSKCheerfulPreditionInfo.toMessageWrapper(): MessageWrapper {
    val currentEventId = ProjectSekaiData.getEventId()

    if (currentEventId != eventId) {
        return buildMessageWrapper {
            appendText("当前活动不是嘉年华活动, 无预测数据")
        }
    }

    return buildMessageWrapper {
        val (team1, team2) = teamName ?: error("Unable to fetch team name")
        val la = getLatestAnnouncement()
        val (p1, p2) = getPredictRate() ?: error("Unable to fetch predict rate")
        val (t1c, t2c) = getMemberCount() ?: error("Unable to fetch member count")

        appendTextln("当前活动 $eventName 对战预测")
        appendLine()
        if (la != null) {
            val (t1p, t2p) = la
            appendTextln("最新中间发表分数 >")
            appendTextln("$team1 $t1p")
            appendTextln("$team2 $t2p")
            appendLine()
        }
        appendTextln("当前队伍人数 > ")
        appendTextln("$team1 $t1c | $team2 $t2c")
        appendTextln("预计胜率 >")
        appendTextln("$team1 ${(p1 * 100).fixDisplay(1)}% | $team2 ${(p2 * 100).fixDisplay(1)}%")
        appendLine()
        appendText("数据来源 33 Kit")
    }
}
