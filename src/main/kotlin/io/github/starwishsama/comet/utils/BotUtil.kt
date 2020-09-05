package io.github.starwishsama.comet.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.coolDown
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.BotUser.Companion.isBotOwner
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.commons.lang3.StringUtils
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

/**
 * 用于辅助机器人运行的各种工具方法
 *
 * @author Nameless
 */

fun MiraiLogger.warningS(message: String?) {
    if (cfg.debugMode) {
        warning(message)
    }
}

fun MiraiLogger.warningS(message: String?, throwable: Throwable?) {
    if (cfg.debugMode) {
        warning(message, throwable)
    }
}

fun MiraiLogger.warningS(throwable: Throwable?) {
    if (cfg.debugMode) {
        warning(throwable)
    }
}

fun MiraiLogger.debugS(message: String?) {
    if (cfg.debugMode) {
        debug(message)
    }
}

fun MiraiLogger.debugS(throwable: Throwable?) {
    if (cfg.debugMode) {
        debug(throwable)
    }
}

fun MiraiLogger.debugS(message: String?, throwable: Throwable?) {
    if (cfg.debugMode) {
        verbose(message, throwable)
    }
}

fun MiraiLogger.verboseS(message: String?) {
    if (cfg.debugMode) {
        verbose(message)
    }
}

fun MiraiLogger.verboseS(throwable: Throwable?) {
    if (cfg.debugMode) {
        verbose(throwable)
    }
}

fun MiraiLogger.verboseS(message: String?, throwable: Throwable?) {
    if (cfg.debugMode) {
        verbose(message, throwable)
    }
}

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
     * 判断指定QQ号是否仍在命令冷却中
     * (可以自定义命令冷却时间)
     *
     * @author Nameless
     * @param qq 要检测的QQ号
     * @param seconds 自定义冷却时间
     * @return 目标QQ号是否处于冷却状态
     */
    fun hasNoCoolDown(qq: Long, seconds: Int = cfg.coolDownTime): Boolean {
        if (seconds < 1) return true

        val currentTime = System.currentTimeMillis()
        if (qq == 80000000L) {
            return false
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

    fun sendMessageAsString(otherText: String?, addPrefix: Boolean = true): String {
        if (otherText.isNullOrEmpty()) return ""
        return StringBuilder().apply {
            if (addPrefix) {
                append(getLocalMessage("msg.bot-prefix")).append(" ")
            }
            append(otherText)
        }.toString().trim()
    }

    fun sendMessage(otherText: String?, addPrefix: Boolean = true): MessageChain = sendMessageAsString(otherText, addPrefix).convertToChain()


    fun sendMessage(vararg otherText: String?, addPrefix: Boolean): MessageChain {
        if (!otherText.isNullOrEmpty()) return "".convertToChain()

        return StringBuilder().apply {
            if (addPrefix) append(getLocalMessage("msg.bot-prefix")).append(" ")
            otherText.forEach {
                append(it).append("\n")
            }
        }.toString().trim().convertToChain()

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
        return StringBuilder().apply {
            if (size == 1) {
                return this[0].toString().trim()
            }

            for (index in startAt until size) {
                append(this[index]).append(" ")
            }
        }.toString().trim()
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
}
