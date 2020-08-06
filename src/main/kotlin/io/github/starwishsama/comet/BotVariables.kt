package io.github.starwishsama.comet

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.Config
import io.github.starwishsama.comet.objects.RandomResult
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.PCRCharacter
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.objects.group.Shop
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.FileUtil
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


/**
 * 机器人(几乎)所有数据的存放类
 * 可以直接访问数据
 * @author Nameless
 */

object BotVariables {
    val filePath: File = FileUtil.getJarLocation()
    const val version = "0.4.3-DEV-3de2e59-20200806"
    lateinit var bot: Bot
    lateinit var startTime: LocalDateTime
    var service: ScheduledExecutorService = Executors.newScheduledThreadPool(
            4,
            BasicThreadFactory.Builder()
                    .namingPattern("bot-service-%d")
                    .daemon(true)
                    .uncaughtExceptionHandler { thread, t ->
                        logger.warning("[定时任务] 线程 ${thread.name} 在运行时发生了错误", t)
                    }.build()
    )
    lateinit var logger: MiraiLogger
    var rCon: Rcon? = null
    lateinit var log: File

    var shop: MutableList<Shop> = LinkedList()
    val users: MutableList<BotUser> = LinkedList()
    var localMessage: MutableList<BotLocalization> = ArrayList()
    var cfg = Config()
    var underCovers: MutableList<RandomResult> = LinkedList()
    var cache: JsonObject = JsonObject()
    val perGroup: MutableSet<PerGroupConfig> = HashSet()

    /** 明日方舟/PCR 卡池数据 */
    var arkNight: MutableList<ArkNightOperator> = LinkedList()
    var pcr: MutableList<PCRCharacter> = LinkedList()

    val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    var hitokoto: Hitokoto? = null

    var switch: Boolean = true

    val coolDown: MutableMap<Long, Long> = HashMap()
}