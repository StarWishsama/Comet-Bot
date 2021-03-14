package io.github.starwishsama.comet.utils.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.starwishsama.comet.BotVariables.yyMMddPattern
import java.time.LocalDateTime

/**
 * [LocalDateTimeConverter]
 *
 * 转换原 Gson 默认序列化 [LocalDateTime] 风格至 Jackson 自定义样式
 */
object LocalDateTimeConverter: JsonDeserializer<LocalDateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        try {
            return LocalDateTime.parse(p.readValueAs(String::class.java), yyMMddPattern)
        } catch (e: Exception) {
            val node = p.readValuesAs(GsonStyleLDT::class.java)

            val ldt = node.next()

            return LocalDateTime.of(
                ldt.date.year,
                ldt.date.month,
                ldt.date.day,
                ldt.time.hour,
                ldt.time.minute,
                ldt.time.second,
                ldt.time.nano
            )
        }
    }
}

/**
 * [GsonStyleLDT]
 *
 * Gson 样式 [LocalDateTime]
 */
data class GsonStyleLDT(
    val date: Date,
    val time: Time
) {
    data class Date(
        val year: Int,
        val month: Int,
        val day: Int
    )

    data class Time(
        val hour: Int,
        val minute: Int,
        val second: Int,
        val nano: Int
    )
}