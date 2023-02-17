package ren.natsuyuk1.comet.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.core.jaroWinklerSimilarity
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.options.validate
import moe.sdl.yac.parameters.types.int
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.command.isGroup
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.objects.command.now.Config
import ren.natsuyuk1.comet.objects.command.now.NowCmdConfigTable
import ren.natsuyuk1.comet.util.newFormatterOrNull
import ren.natsuyuk1.comet.util.newTimeZoneOrNull
import ren.natsuyuk1.comet.util.toMessageWrapper
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration.Companion.minutes

val NOW = CommandProperty(
    "now",
    listOf("time", "时间"),
    "查询时间信息",
    "/now 查询现在时间",
)

private object Format {
    val date = DateTimeFormatter.ISO_LOCAL_DATE
    val hms = DateTimeFormatter.ofPattern("HH:mm:ss")
    val hm = DateTimeFormatter.ofPattern("HH:mm")
    val datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}

class NowCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    user: CometUser,
) : CometCommand(comet, sender, subject, message, user, NOW) {
    companion object {
        // ************************************** $1 * $2 * $3 *******
        private val aliasRegex = Regex("""^(.+?)(==>(.+?))?${'$'}""")
    }

    val search by option("-S", "--search")
    val searchNum by option("-n", "--search-num").int().validate {
        require(it in 1..20)
    }
    val formatter by option("-F", "--formatter")
    val save by option("-s", "--save").flag()
    val timezone by option("-Z", "--zone").convert { opt ->
        opt.split(",").filterNot { it.isBlank() }.map {
            fun failed(): Nothing = throw PrintMessage("解析时区表达式失败 $it")
            val result = aliasRegex.matchEntire(it.trim()) ?: failed()
            val zone = result.groups[1]?.value ?: failed()
            val alias = result.groups[3]?.value
            zone to alias
        }
    }

    suspend fun formatter(): DateTimeFormatter? = withContext(Dispatchers.IO) c@{
        val formatter = formatter ?: newSuspendedTransaction {
            NowCmdConfigTable.getConfig(sender.platform, subject.isGroup(), subject.id)
        }?.formatter
        val low = formatter?.lowercase()
        when {
            formatter == null -> Format.hms
            low == "date" || low == "d" -> Format.date
            low == "time" || low == "t" || low == "hms" -> Format.hms
            low == "hm" -> Format.hm
            low == "datetime" || low == "dt" -> Format.datetime
            else -> newFormatterOrNull(formatter)
        }
    }

    suspend fun timezone(): List<Pair<TimeZone, String?>> = withContext(Dispatchers.IO) c@{
        val timezone = timezone
        if (timezone.isNullOrEmpty()) {
            val timezones = newSuspendedTransaction {
                NowCmdConfigTable.getConfig(sender.platform, subject.isGroup(), subject.id)
            }?.timezones
            return@c timezones ?: listOf(TimeZone.getDefault() to null)
        }
        timezone.map {
            (newTimeZoneOrNull(it.first) ?: throw PrintMessage("解析时区失败 ${it.first}")) to it.second
        }
    }

    private fun similarity(word: String, possibleValues: Array<String>, take: Int = 5) =
        possibleValues.asSequence()
            .filter { word.first() == it.first() }
            .map { it to jaroWinklerSimilarity(word, it) }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(take)

    override suspend fun run() = withContext(Dispatchers.IO) c@{
        if (search != null) {
            val possible = similarity(search!!, TimeZone.getAvailableIDs(), searchNum ?: 5).joinToString()
            TimeZone.getAvailableIDs().map { TimeZone.getTimeZone(it) }.map { it.displayName }
            subject.sendMessage("搜索结果: $possible".toMessageWrapper())
            return@c
        }

        val dateFormatter = async {
            formatter() ?: throw PrintMessage("解析格式化模板失败")
        }
        val now = Clock.System.now().toJavaInstant()

        val timezones = async {
            timezone()
        }

        if (save) {
            newSuspendedTransaction {
                val config = Config(formatter, timezones.await())
                NowCmdConfigTable.setConfig(sender.platform, subject.isGroup(), subject.id, config)
            }

            subject.sendMessage("已将当前参数保存到设置".toMessageWrapper())

            return@c
        }

        val timeMessage = async {
            val tz = timezones.await().map { (tz, alias) ->
                val name = alias ?: tz.id
                val zone = now.atZone(tz.toZoneId()).format(dateFormatter.await())
                name to zone
            }

            val maxLen = tz.maxOfOrNull { it.first.length }
            tz.joinToString("\n") { (name, time) ->
                "${name.padEnd(maxLen ?: 0, '　')} > $time"
            }
        }

        subject.sendMessage(
            buildMessageWrapper {
                appendTextln(timeMessage.await())
            },
        )?.delayDelete(1.minutes)

        if (subject is Group && subject.getBotPermission() != GroupPermission.MEMBER) {
            message.receipt?.delayDelete(1.minutes)
        }
    }
}
