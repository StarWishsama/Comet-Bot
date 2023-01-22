package ren.natsuyuk1.comet.objects.config

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.Text
import ren.natsuyuk1.comet.api.message.toMessageWrapper
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object AliasCommandHandler {
    /**
     * 处理命令别名
     *
     * @param message 传入消息
     */
    suspend fun handle(
        comet: Comet,
        sender: PlatformCommandSender,
        subject: PlatformCommandSender,
        message: MessageWrapper
    ) {
        val content = message.encodeToString()

        val altarKey = AliasConfig.data.alias.keys.find {
            content.startsWith(it)
        } ?: return

        val altar = AliasConfig.data.alias[altarKey] ?: return

        val processIndex = message.getMessageContent().indexOfFirst {
            it is Text && it.text.startsWith(altarKey)
        }

        val convert = message.getMessageContent().toMutableList()
        (convert[processIndex] as? Text)?.apply {
            convert[processIndex] = Text(text.replace(altarKey, altar.cmd))
        }

        CommandManager.executeCommand(
            comet,
            sender,
            subject,
            convert.toMessageWrapper(),
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
