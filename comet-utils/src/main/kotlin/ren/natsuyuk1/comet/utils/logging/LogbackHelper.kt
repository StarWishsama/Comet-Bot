/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.utils.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.pattern.ClassOfCallerConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class LogbackHelper {
    /**
     * Optimize logger class name for Kotlin.
     * Original [PatternLayoutEncoder] will encode class name with Kotlin lambda.
     * It's redundant and useless.
     *
     * So [PatternLayoutNoLambda] split the name by `$` and remove text after it.
     *
     * Before `SorapointaMain$run$inline$2$1` -> Now `SorapointaMain`
     *
     * @see [ClassOfCallerConverterNoLambda]
     */
    class PatternLayoutNoLambda : PatternLayoutEncoder() {
        init {
            val name = ClassOfCallerConverterNoLambda::class.java.name
            PatternLayout.DEFAULT_CONVERTER_MAP["C"] = name
            PatternLayout.DEFAULT_CONVERTER_MAP["class"] = name
            PatternLayout.CONVERTER_CLASS_TO_KEY_MAP[name] = "class"
        }
    }

    class ClassOfCallerConverterNoLambda : ClassOfCallerConverter() {
        override fun getFullyQualifiedName(event: ILoggingEvent?): String {
            val name = super.getFullyQualifiedName(event)
            return name.split('$').firstOrNull().toString()
        }
    }
}
