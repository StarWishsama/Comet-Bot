package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.delay
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.registerTimeout
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GithubCommandService {
    private val repoRegex = "(\\w*)/(\\w*)".toRegex()

    // 3, 4
    private val githubLinkRegex by lazy { Regex("""^(https?://)?(www\.)?github\.com/(\w+)/(\w+)$""") }

    class GitHubSubscribeSession(
        contact: PlatformCommandSender,
        val sender: PlatformCommandSender,
        user: CometUser,
        val owner: String,
        val name: String,
        private val groupID: Long,
    ) : Session(contact, user) {
        override fun handle(message: MessageWrapper) {
            val raw = message.parseToString()
            val secret = if (raw == "完成订阅") {
                ""
            } else {
                raw
            }

            GithubRepoData.data.repos.add(
                GithubRepoData.Data.GithubRepo(
                    name,
                    owner,
                    secret,
                    mutableListOf(GithubRepoData.Data.GithubRepo.GithubRepoSubscriber(groupID))
                )
            )

            sender.sendMessage("订阅仓库 $owner/$name 成功, 请至仓库 WebHook 设置添加 Comet 管理提供的链接!".toMessageWrapper())

            expire()
        }
    }

    suspend fun processSubscribe(
        subject: PlatformCommandSender,
        sender: PlatformCommandSender,
        user: CometUser,
        groupID: Long,
        repoName: String
    ) {
        if (repoName.matches(repoRegex)) {
            val slice = repoName.split("/")
            val owner = slice[0];
            val name = slice[1]

            val repos = GithubRepoData.data.repos

            if (repos.any { it.getName() == repoName && it.subscribers.any { sub -> sub.id == groupID } }) {
                subject.sendMessage("你已经订阅过这个仓库了!".toMessageWrapper())
                return
            }

            if (GitHubApi.isRepoExist(owner, name)) {
                val repo = GithubRepoData.data.repos.find { it.getName() == "$owner/$name" }

                if (repo == null) {
                    if (subject is Group) {
                        subject.sendMessage("请在私聊中继续完成你的订阅".toMessageWrapper())
                    }

                    delay(1.seconds)

                    sender.sendMessage(
                        ("你正在订阅仓库 $owner/$name, 是否需要添加仓库机密 (Secret)?\n" +
                            "添加机密后, 能够使接收仓库信息更加安全, 但千万别忘记了!\n" +
                            "如果无需添加, 请回复「完成订阅」, 反之直接发送你欲设置的机密.").toMessageWrapper()
                    )

                    GitHubSubscribeSession(subject, sender, user, owner, name, groupID).registerTimeout(1.minutes)
                } else {
                    repo.subscribers.add(GithubRepoData.Data.GithubRepo.GithubRepoSubscriber(groupID))
                    subject.sendMessage("订阅仓库 $owner/$name 成功, 请至仓库 WebHook 设置添加 Comet 管理提供的链接!".toMessageWrapper())
                }
            } else {
                subject.sendMessage("找不到你想要订阅的 GitHub 仓库".toMessageWrapper())
            }
        } else {
            subject.sendMessage("请输入有效的 GitHub 仓库名称, 例如 StarWishsama/Comet-Bot".toMessageWrapper())
        }
    }
}
