package io.github.starwishsama.comet.utils

import cn.hutool.core.io.IORuntimeException
import cn.hutool.http.HttpResponse
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.coolDown
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.BotUser.Companion.isBotAdmin
import io.github.starwishsama.comet.objects.BotUser.Companion.isBotOwner
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.asHumanReadable
import org.apache.commons.lang3.StringUtils
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

/**
 * 将字符串转换为消息链
 */
fun String.toMsgChain(): MessageChain {
    return toMessage().asMessageChain()
}

fun String.isOutRange(range: Int) : Boolean {
    return length > range
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

/**
 * 来自 Mirai 的 [asHumanReadable]
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

fun HttpResponse.isType(typeName: String): Boolean {
    val contentType = this.header("content-type") ?: return false
    return contentType.contains(typeName)
}

/**
 * 用于辅助机器人运行的各种工具方法
 *
 * @author Nameless
 */

object BotUtil {
    /**
     * 判断是否签到过了
     *
     * @author NamelessSAMA
     * @param user 机器人账号
     * @return 是否签到
     */
    fun isChecked(user: BotUser): Boolean {
        val now = LocalDateTime.now()
        val period = user.lastCheckInTime.toLocalDate().until(now.toLocalDate())

        return period.days == 0
    }

    /**
     * 判断指定QQ号是否仍在冷却中
     *
     * @author NamelessSAMA
     * @param qq 指定的QQ号
     * @return 目标QQ号是否处于冷却状态
     */
    fun isNoCoolDown(qq: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        if (qq == 80000000L) {
            return false
        }

        if (qq == BotVariables.cfg.ownerId) {
            return true
        }

        if (coolDown.containsKey(qq) && !isBotAdmin(qq)) {
            val cd = coolDown[qq]
            if (cd != null) {
                if (currentTime - cd < BotVariables.cfg.coolDownTime * 1000) {
                    return false
                } else {
                    coolDown.remove(qq)
                }
            }
        } else {
            coolDown[qq] = currentTime
        }
        return true
    }

    /**
     * 判断指定QQ号是否仍在冷却中
     * (可以自定义命令冷却时间)
     *
     * @author Nameless
     * @param qq 要检测的QQ号
     * @param seconds 自定义冷却时间
     * @return 目标QQ号是否处于冷却状态
     */
    fun isNoCoolDown(qq: Long, seconds: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        if (qq == 80000000L) {
            return false
        }

        if (qq == BotVariables.cfg.ownerId) {
            return true
        }

        if (coolDown.containsKey(qq) && !isBotOwner(qq)) {
            if (currentTime - coolDown[qq]!! < seconds * 1000) {
                return false
            } else {
                coolDown.remove(qq)
            }
        } else {
            coolDown[qq] = currentTime
        }
        return true
    }

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
     * 获取本地化文本
     *
     * @author NamelessSAMA
     * @param node 本地化文本节点
     * @return 本地化文本
     */
    fun getLocalMessage(node: String): String {
        for ((n, t) in BotVariables.localMessage) {
            if (n.contentEquals(node)) {
                return t
            }
        }
        return "PlaceHolder"
    }

    /**
     * 获取带有本地化文本的消息
     *
     * @author NamelessSAMA
     * @param node 本地化文本节点
     * @param otherText 需要添加的文本
     * @return 本地化文本
     */
    fun sendLocalMessage(node: String, otherText: String): String {
        return getLocalMessage(node) + otherText
    }

    /**
     * 获取带有本地化文本的消息
     *
     * @author NamelessSAMA
     * @param node 本地化文本节点
     * @param otherText 需要添加的文本
     * @return 本地化文本
     */
    fun sendLocalMessage(node: String, vararg otherText: String): String {
        val sb = StringBuilder()
        sb.append(getLocalMessage(node)).append(" ")
        for (s in otherText) {
            sb.append(s).append("\n")
        }
        return sb.toString().trim { it <= ' ' }
    }

    /**
     * 发送带前缀的消息
     *
     * @author NamelessSAMA
     * @param otherText 需要添加的文本
     * @return 本地化文本
     */
    fun sendMsgPrefix(vararg otherText: String): String {
        val sb = StringBuilder()
        sb.append(getLocalMessage("msg.bot-prefix")).append(" ")
        otherText.forEach {
            sb.append(it).append("\n")
        }
        return sb.toString().trim { it <= ' ' }
    }

    fun sendMessage(otherText: String?, addPrefix: Boolean = true): MessageChain {
        if (otherText.isNullOrEmpty()) return EmptyMessageChain
        val sb = StringBuilder()
        if (addPrefix) sb.append(getLocalMessage("msg.bot-prefix")).append(" ")
        sb.append(otherText)
        return sb.toString().trim { it <= ' ' }.toMsgChain()
    }

    fun sendMessage(vararg otherText: String?, addPrefix: Boolean): MessageChain {
        if (!otherText.isNullOrEmpty()) return "".toMsgChain()

        val sb = StringBuilder()
        if (addPrefix) sb.append(getLocalMessage("msg.bot-prefix")).append(" ")
        otherText.forEach {
            sb.append(it).append("\n")
        }
        return sb.toString().trim { it <= ' ' }.toMsgChain()
    }

    /**
     * 发送带前缀的消息, 兼容空的字符串
     *
     * @author NamelessSAMA
     * @param otherText 需要添加的文本
     * @return 本地化文本
     */
    fun sendMsgPrefixOrEmpty(otherText: String?): String {
        return if (!otherText.isNullOrEmpty()) {
            val sb = StringBuilder()
            sb.append(getLocalMessage("msg.bot-prefix")).append(" ")
            otherText.forEach {
                sb.append(it).append("\n")
            }
            sb.toString().trim { it <= ' ' }
        } else {
            ""
        }
    }

    /**
     * 获取用户的权限组等级
     *
     * @author NamelessSAMA
     * @param qq 指定用户的QQ号
     * @return 权限组等级
     */
    fun getLevel(qq: Long): UserLevel {
        val user = BotUser.getUser(qq)
        if (user != null) {
            return user.level
        }
        return UserLevel.USER
    }

    fun List<String>.getRestString(startAt: Int): String {
        val sb = StringBuilder()
        if (this.size == 1) {
            return this[0]
        }

        for (index in startAt until this.size) {
            sb.append(this[index]).append(" ")
        }
        return sb.toString().trim()
    }

    @ExperimentalTime
    fun getRunningTime(): String {
        val remain = Duration.between(BotVariables.startTime, LocalDateTime.now())
        return remain.toKotlinDuration().toFriendly(TimeUnit.DAYS)
    }

    fun getAt(event: MessageEvent, id: String) : BotUser? {
        val at = event.message[At]

        return if (at != null) {
            BotUser.getUser(at.target)
        } else {
            if (StringUtils.isNumeric(id)) {
                BotUser.getUser(id.toLong())
            } else {
                null
            }
        }
    }

    @ExperimentalTime
    fun getMemoryUsage(): String =
        "OS 信息: ${getOsInfo()}\n" +
                "JVM 版本: ${getJVMVersion()}\n" +
                "内存占用: ${getUsedMemory()}MB/${getMaxMemory()}MB\n" +
                "运行时长: ${getRunningTime()}"

    fun returnMsgIf(condition: Boolean, msg: MessageChain): MessageChain = if (condition) msg else EmptyMessageChain

    fun returnMsgIfElse(condition: Boolean, msg: MessageChain, default: MessageChain): MessageChain {
        if (condition) {
            return msg
        }
        return default
    }

    fun isValidJson(json: String): Boolean {
        val jsonElement: JsonElement = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            return false
        }
        return jsonElement.isJsonObject
    }

    fun isValidJson(element: JsonElement): Boolean {
        return element.isJsonObject || element.isJsonArray
    }

    fun executeWithRetry(task: () -> Unit, retryTime: Int, message: String) {
        if (retryTime >= 5) return

        var initRetryTime = 0
        fun runTask(): () -> Unit = {
            try {
                if (initRetryTime <= retryTime) {
                    task()
                }
            } catch (t: Throwable) {
                if (t is IORuntimeException) {
                    initRetryTime++
                    runTask()()
                } else {
                    logger.warning(message, t)
                }
            }
        }

        runTask()()
    }
}
