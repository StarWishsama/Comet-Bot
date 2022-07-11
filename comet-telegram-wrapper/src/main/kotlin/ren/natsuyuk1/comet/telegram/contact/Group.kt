package ren.natsuyuk1.comet.telegram.contact

/**fun TextHandlerEnvironment.toCometGroup(comet: TelegramComet): Group {
if (message.chat.type != "group" && message.chat.type != "supergroup") {
error("Cannot cast a non-group chat to Comet Group")
}

val chat = this@toCometGroup

return object : Group(
message.chat.id,
message.chat.username ?: "未知群聊",
) {
override fun updateGroupName(groupName: String) {
error("You cannot update group name in telegram!")
}

override fun getBotMuteRemaining(): Int = -1

override fun getBotPermission(): GroupPermission {
TODO("Not yet implemented")
}

override val avatarUrl: String
get() = TODO("Not yet implemented")

override fun getMember(id: Long): GroupMember? {
TODO("Not yet implemented")
}

override suspend fun quit(): Boolean {
TODO("Not yet implemented")
}

override fun contains(id: Long): Boolean {
TODO("Not yet implemented")
}

override val comet: Comet
get() = comet
override var card: String
get() = TODO("Not yet implemented")
set(value) {}

override fun sendMessage(message: MessageWrapper) {
comet.bot.sendMessage(ChatId.fromId(this@toCometGroup.message.chat.id), message.parseToString())
}
}
}*/
