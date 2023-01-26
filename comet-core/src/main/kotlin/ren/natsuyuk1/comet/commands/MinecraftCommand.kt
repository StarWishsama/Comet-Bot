package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import moe.sdl.yac.parameters.types.int
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.network.thirdparty.minecraft.MinecraftServerType
import ren.natsuyuk1.comet.network.thirdparty.minecraft.QueryInfo
import ren.natsuyuk1.comet.network.thirdparty.minecraft.query
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.srv.SRVLookup

val MINECRAFT = CommandProperty(
    "minecraft",
    listOf("mc", "我的世界"),
    "查询 Minecraft 服务器信息",
    "/mc [服务器地址] (端口) (-p java|bedrock 服务器类型)"
)

class MinecraftCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, MINECRAFT) {
    private val protocol by option("-p", "--protocol", help = "服务器协议")
        .enum<MinecraftServerType>(ignoreCase = true).default(MinecraftServerType.JAVA)

    private val host by argument("服务器地址")
    private val port by argument("服务器端口")
        .int()
        .default(0)

    override suspend fun run() {
        var result: QueryInfo?
        val actualPort = if (port == 0) {
            if (protocol == MinecraftServerType.JAVA) 25565
            else 19132
        } else port

        result = try {
            val srvRecord = SRVLookup.lookup(host, "minecraft")

            if (srvRecord != null) {
                query(srvRecord.first, srvRecord.second, protocol)
            } else {
                query(host, actualPort, protocol)
            }
        } catch (e: Exception) {
            query(host, actualPort, protocol)
        }

        if (result == null) {
            subject.sendMessage("电波传达不到哦, 可能服务器关服了?".toMessageWrapper())
        } else {
            subject.sendMessage(result.toMessageWrapper())
        }
    }
}
