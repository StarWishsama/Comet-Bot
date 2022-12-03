package ren.natsuyuk1.comet.commands

import cn.hutool.core.util.HexUtil
import cn.hutool.crypto.Mode
import cn.hutool.crypto.Padding
import cn.hutool.crypto.symmetric.AES
import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.enum
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.util.toMessageWrapper

val ENCRYPT_TOOL = CommandProperty(
    "encrypttool",
    listOf("etool"),
    "加密工具包",
    ""
)

class EncryptToolCommand(
    comet: Comet,
    sender: PlatformCommandSender,
    subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser,
) : CometCommand(comet, sender, subject, message, user, ENCRYPT_TOOL) {
    init {
        subcommands(
            AESCommand(subject, sender, user)
        )
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            return
        }
    }

    class AESCommand(
        subject: PlatformCommandSender,
        sender: PlatformCommandSender,
        user: CometUser,
    ) : CometSubCommand(subject, sender, user, AES) {
        companion object {
            val AES = SubCommandProperty("aes", listOf(), ENCRYPT_TOOL)
        }

        private val key by option("--key", "-k", help = "key")
        private val iv by option("--iv", help = "iv")
        private val mode by option("--mode", "-m", help = "模式").enum<Mode>()
        private val raw by option("--raw", "-r", help = "原始输出").flag()
        private val content by argument("加密内容")

        override suspend fun run() {
            if (mode == null) {
                subject.sendMessage("请提供加密模式!".toMessageWrapper())
                return
            }

            val aes = if (key != null && iv != null) {
                AES(mode, Padding.PKCS5Padding, key!!.toByteArray(), iv!!.toByteArray())
            } else if (key != null) {
                AES(mode, Padding.PKCS5Padding, key!!.toByteArray())
            } else {
                AES()
            }

            if (raw) {
                subject.sendMessage(HexUtil.encodeHexStr(aes.encrypt(content)).toMessageWrapper(""))
            } else {
                subject.sendMessage(aes.encryptBase64(content).toMessageWrapper(""))
            }
        }
    }
}
