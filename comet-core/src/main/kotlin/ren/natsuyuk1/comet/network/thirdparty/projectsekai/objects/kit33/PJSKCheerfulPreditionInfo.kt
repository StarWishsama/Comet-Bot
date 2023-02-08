package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33

import kotlinx.serialization.json.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper

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

fun PJSKCheerfulPreditionInfo.toMessageWrapper(): MessageWrapper? =
    buildMessageWrapper {
        val (team1, team2) = teamName ?: return null
        val (t1p, t2p) = getLatestPoint() ?: return null

        setUsable(true)
        appendTextln("当前活动 $eventName")
        appendLine()
        appendTextln("$team1 :")
    }
