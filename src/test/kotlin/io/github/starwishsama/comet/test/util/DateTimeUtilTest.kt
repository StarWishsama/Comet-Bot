package io.github.starwishsama.comet.test.util

import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.gaokaoDateTime
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class DateTimeUtilTest {
    @Test
    fun gaokaoTimePrintTest() {
        println(gaokaoDateTime.getLastingTimeAsString(TimeUnit.DAYS))
    }
}
