package io.github.starwishsama.nbot.utils

import cn.hutool.core.io.file.FileReader
import cn.hutool.core.io.file.FileWriter
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.BotUser.Companion.isBotAdmin
import io.github.starwishsama.nbot.objects.BotUser.Companion.isBotOwner
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * 将字符串转换为消息链
 */
fun String.toMirai(): MessageChain {
    return toMessage().asMessageChain()
}

fun String.isOutRange(range: Int) : Boolean {
    return length > range
}

fun File.writeJson(context: Any) {
    synchronized(this) {
        if (!this.exists()) {
            this.createNewFile()
        }

        FileWriter.create(this).write(BotConstants.gson.toJson(context))
    }
}

fun File.writeString(context: String) {
    synchronized(this) {
        if (!this.exists()) {
            this.createNewFile()
        }

        FileWriter.create(this).write(context)
    }
}

fun File.getContext(): String {
    return FileReader.create(this).readString()
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
 * 用于辅助机器人运行中的各种工具方法
 *
 * @author Nameless
 */

object BotUtil {
    /** 冷却 */
    private var coolDown: MutableMap<Long, Long> = HashMap()

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

        if (qq == BotConstants.cfg.ownerId){
            return true
        }

        if (coolDown.containsKey(qq) && !isBotAdmin(qq)) {
            val cd = coolDown[qq]
            if (cd != null) {
                if (currentTime - cd < BotConstants.cfg.coolDownTime * 1000) {
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

        if (qq == BotConstants.cfg.ownerId){
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
        for ((n, t) in BotConstants.msg) {
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

    fun getRunningTime(): String {
        val remain = Duration.between(BotMain.startTime, LocalDateTime.now())
        return "${remain.toDaysPart()}天${remain.toHoursPart()}时${remain.toMinutesPart()}分${remain.toSecondsPart()}秒${remain.toMillisPart()}毫秒"
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

    fun getMemoryUsage(): String = "操作系统: ${getOsName()}\n" +
            "JVM 版本: ${getJVMVersion()}\n" +
            "内存占用: ${getUsedMemory()}MB/${getMaxMemory()}MB\n" +
            "运行时长: ${getRunningTime()}"

    fun returnMsgIf(condition: Boolean, msg: MessageChain): MessageChain {
        if (condition) {
            return msg
        } else {
            throw UnsupportedOperationException()
        }
    }

    fun returnMsgIfElse(condition: Boolean, msg: MessageChain, default: MessageChain): MessageChain {
        if (condition) {
            return msg
        }
        return default
    }

    fun isValidJson(json: String): Boolean {
        val jsonElement: JsonElement? = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            return false
        }
        return jsonElement?.isJsonObject ?: false
    }

    fun isValidJson(element: JsonElement?): Boolean {
        return element?.isJsonObject ?: false
    }
}
