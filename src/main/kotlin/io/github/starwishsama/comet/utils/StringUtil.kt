package io.github.starwishsama.comet.utils

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.asMessageChain
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

object StringUtil {
    /**
     * 判断ID是否符合育碧账号昵称格式规范
     *
     * @author NamelessSAMA
     * @param username 用户名
     * @return 是否符合规范
     */
    fun isLegitId(username: String): Boolean {
        return username.matches(Regex("[a-zA-Z0-9_.-]*"))
    }

    /**
     * 来自 Mirai 的 asHumanReadable
     */
    @ExperimentalTime
    fun kotlin.time.Duration.toFriendly(maxUnit: DurationUnit = TimeUnit.SECONDS): String {
        val days = toInt(DurationUnit.DAYS)
        val hours = toInt(DurationUnit.HOURS) % 24
        val minutes = toInt(DurationUnit.MINUTES) % 60
        val seconds = (toInt(DurationUnit.SECONDS) % 60 * 1000) / 1000
        val ms = (toInt(DurationUnit.MILLISECONDS) % 60 * 1000 * 1000) / 1000 / 1000
        return buildString {
            if (days != 0 && maxUnit >= TimeUnit.DAYS) append("${days}天")
            if (hours != 0 && maxUnit >= TimeUnit.HOURS) append("${hours}时")
            if (minutes != 0 && maxUnit >= TimeUnit.MINUTES) append("${minutes}分")
            if (seconds != 0 && maxUnit >= TimeUnit.SECONDS) append("${seconds}秒")
            append("${ms}毫秒")
        }
    }

    /**
     * 将字符串转换为消息链
     */
    fun String.convertToChain(): MessageChain {
        return PlainText(this).asMessageChain()
    }

    /**
     * 判断字符串是否为整数
     * @return 是否为整数
     */
    fun String.isNumeric(): Boolean {
        return matches("[-+]?\\d*\\.?\\d+".toRegex()) && !this.contains(".")
    }

    fun String.limitStringSize(size: Int): String {
        return if (length <= size) this else substring(0, size) + "..."
    }
}