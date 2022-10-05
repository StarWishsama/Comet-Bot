package ren.natsuyuk1.comet.commands

import inet.ipaddr.HostName
import inet.ipaddr.IPAddressNetwork.IPAddressGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import moe.sdl.ipdb.Reader
import moe.sdl.ipdb.parser.builtins.FullInfo
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.optional
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.objects.config.IpdbConfig
import ren.natsuyuk1.comet.util.sendMessage
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimeZonePattern
import java.net.IDN
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

val IP by lazy {
    CommandProperty(
        "ip", emptyList(), "查询 IP 或域名归属", "/ip <IP>"
    )
}

private val infoFields by lazy {
    FullInfo::class.memberProperties.filter {
        it.returnType.jvmErasure == String::class
    }
}

private val delDuration = 60.toDuration(DurationUnit.SECONDS)

class IPCommand(
    comet: Comet,
    sender: PlatformCommandSender,
    subject: PlatformCommandSender,
    private val message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, IP) {
    private val link by argument("LINK", "IP 或域名").optional()
    private val language by option("-l", "--language", help = "要显示的语言")
    private val position by option("-p", "--position", help = "是否显示经纬度").flag()
    private val verbose by option("-v", "--verbose", help = "是否显示详细信息").flag()
    private val metadata by option("-m", "--metadata", help = "输出元数据").flag()

    override suspend fun run() {
        if (!IpdbConfig.data.enable) return

        if (metadata) {
            val dbs = listOfNotNull(IpdbConfig.data.dbV4.get(), IpdbConfig.data.dbV6.get())
            if (dbs.isEmpty()) {
                subject.sendMessage("当前机器人未配置 IPDB")
                return
            }
            subject.sendMessage("\n" + dbs.toMetadataString())?.delayDelete(delDuration)
            message.receipt?.delayDelete(delDuration)
            return
        }

        if (link == null) {
            subject.sendMessage("错误: 缺少参数 \"LINK\"")
            return
        }

        val host = HostName(link)
        val addr = if (host.isAddress) {
            host.asAddress()
        } else {
            val idn = try {
                IDN.toASCII(link)
            } catch (e: IllegalArgumentException) {
                subject.sendMessage("输入了无效的域名")
                logger.info(e) { "Failed to convert argument to IDN format" }
                return
            }

            if (idn.length !in 2..253) {
                subject.sendMessage("IP 或域名长度超出限制 [2, 253]")
                return
            }
            val dnsQueried = withTimeoutOrNull(5_000) {
                withContext(Dispatchers.IO) {
                    try {
                        InetAddress.getByName(idn)
                    } catch (e: UnknownHostException) {
                        logger.info(e) { "Failed to parse host to ip: $idn" }
                        throw PrintMessage("解析域名失败")
                    }
                }
            } ?: throw PrintMessage("解析域名超时")
            IPAddressGenerator().from(dnsQueried)
        }

        val db = try {
            when {
                addr.isIPv4 -> IpdbConfig.data.dbV4.get()
                addr.isIPv6 -> IpdbConfig.data.dbV6.get()
                else -> {
                    subject.sendMessage("无效的地址: $addr")
                    return
                }
            }
        } catch (e: Exception) {
            subject.sendMessage("读取 IPDB 文件失败")
            logger.debug(e) { "Failed to read IPDB file" }
            return
        }

        if (db == null) {
            subject.sendMessage("当前机器人不支持或未配置 ${addr.ipVersion} IPDB")
            return
        }

        val lang = language ?: if (db.metadata.languages.containsKey("CN")) "CN" else db.metadata.languages.keys.first()

        val info = try {
            db.findThenParse(FullInfo, addr, lang)
        } catch (e: IllegalArgumentException) {
            subject.sendMessage("内部错误: ${e.message}")
            logger.warn(e) { "IP version is different" }
            return
        }
        if (info == null) {
            subject.sendMessage("数据库中不存在该 IP 记录: $addr")
            return
        }

        val str = buildString {
            appendLine("结果为:")
            if (verbose) {
                infoFields.forEach {
                    val value = it.getter(info) as? String
                    if (!value.isNullOrBlank()) {
                        appendLine("${it.name}: $value")
                    }
                }
            } else {
                appendLine(info.toMetadataString())
                if (position && info.longitude.isNotBlank() && info.latitude.isNotBlank()) {
                    appendLine("经纬度: ${info.latitude}, ${info.longitude}")
                }
            }
        }

        message.receipt?.delayDelete(delDuration)
        subject.sendMessage(str)?.delayDelete(delDuration)
    }
}

private inline val FullInfo.isIDC: Boolean
    get() = idc == "IDC"

private inline val FullInfo.isAnyCast: Boolean
    get() = idc == "ANYCAST"

private inline val FullInfo.euDesc: String
    get() = if (europeanUnion == "1") "欧盟" else ""

// add " $this" if not null or empty
private val String.sp
    get() = if (!isNullOrBlank()) " $this" else ""

private val FullInfo.regionNoDep: String
    get() = if (regionName != countryName) regionName else ""

/* ktlint-disable max-line-length */
private fun FullInfo.toMetadataString(): String = when {
    isAnyCast -> "任播网络${if (isIDC) " IDC" else ""} 所有者域名: $ownerDomain | ISP 域名：$ispDomain"
    isIDC -> "IDC${ispDomain.sp}${ownerDomain.sp} | ${euDesc.sp}${countryName}${regionNoDep}${cityName}$districtName"
    else -> "${euDesc.sp}${countryName}${regionNoDep}${cityName}${districtName}${ispDomain.sp}${ownerDomain.sp}"
} /* ktlint-enable max-line-length */

fun List<Reader>.toMetadataString(): String = buildString {
    this@toMetadataString.map {
        it.ipVersion to it.metadata
    }.forEach { (ver, meta) ->
        append(" - ")
        append(ver?.name ?: "未知")
        append(" IPDB")
        appendLine(": ")
        val format = Instant
            .ofEpochSecond(meta.build)
            .atZone(ZoneId.of("UTC"))
            .format(yyMMddWithTimeZonePattern)
        appendLine("   构建时间: $format")
        append("   支持的语言: ")
        appendLine(meta.languages.keys.joinToString(", "))
        append("   支持的字段: ")
        appendLine(meta.fields.joinToString(", "))
    }
}
