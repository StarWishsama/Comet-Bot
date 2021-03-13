package io.github.starwishsama.comet.utils.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.starwishsama.comet.BotVariables.yyMMddPattern
import java.time.LocalDateTime

/**
 * [LocalDateTimeSupport]
 *
 *
 */
object LocalDateTimeSupport: JsonDeserializer<LocalDateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        try {
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
        } catch (e: Exception) {
            return LocalDateTime.parse(p.readValueAs(String::class.java), yyMMddPattern)
        }
    }
}

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