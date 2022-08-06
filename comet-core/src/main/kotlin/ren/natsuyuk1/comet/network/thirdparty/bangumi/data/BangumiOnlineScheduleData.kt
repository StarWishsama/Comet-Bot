package ren.natsuyuk1.comet.network.thirdparty.bangumi.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import ren.natsuyuk1.comet.utils.datetime.toChinese
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.time.hourMinutePattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class BangumiOnlineScheduleData(
    val code: Int,
    @SerialName("msg")
    val message: String,
    val data: List<JsonObject>
) {
    /**
     * 获取指定星期几下更新的番剧
     *
     * 注意: 节点名为自 8:00 以来经过的**秒数**，需要经过转换才是实际时间
     */
    fun getSpecificDaySchedule(day: DayOfWeek = LocalDate.now().dayOfWeek): MessageWrapper {
        val bangumis = data[day.value - 1].entries

        return buildMessageWrapper {
            appendText("${day.toChinese()} 新番列表 >>", true)

            bangumis.forEach { (k, v) ->
                appendText("${k.toLong().convertToTime()} => ")
                appendText(
                    buildString {
                        v.jsonArray.forEach bgmName@{ ele ->
                            ele.jsonObject["title_zh"]?.jsonPrimitive?.contentOrNull.let {
                                if (it != null) {
                                    append("$it | ")
                                    return@bgmName
                                }
                            }

                            ele.jsonObject["title_ja"]?.jsonPrimitive?.contentOrNull?.let {
                                append("$it | ")
                            }
                        }
                    }.removeSuffix(" | ")
                )
                appendLine()
            }
        }
    }

    private fun Long.convertToTime(): String {
        val dummyTime = LocalTime.of(0, 0)

        val convert = dummyTime.plusSeconds(this)

        return hourMinutePattern.format(convert)
    }
}
