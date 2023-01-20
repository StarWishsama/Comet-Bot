package ren.natsuyuk1.comet.objects.config

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object AliasCommandHandler {
    /**
     * 处理命令别名
     *
     * @param message 传入消息
     *
     */
    suspend fun handle(
        comet: Comet,
        sender: PlatformCommandSender,
        subject: PlatformCommandSender,
        message: MessageWrapper
    ) {
        val content = message.encodeToString()

        val altar = AliasConfig.data.alias[content] ?: return

        CommandManager.executeCommand(
            comet,
            sender,
            subject,
            buildMessageWrapper {
                appendText(altar.cmd)
            },
            Pair(altar.userLevel, altar.permission)
        )
    }
}

object AliasConfig : PersistDataFile<AliasConfig.Data>(
    File(configDirectory, "alias.json"),
    Data.serializer(),
    Data(),
    readOnly = true
) {
    @Serializable
    data class Data(
        val alias: MutableMap<String, AliasCommand> = mutableMapOf()
    )
}

@Serializable
data class AliasCommand(
    val cmd: String,
    val permission: String = "",
    val userLevel: UserLevel
)
