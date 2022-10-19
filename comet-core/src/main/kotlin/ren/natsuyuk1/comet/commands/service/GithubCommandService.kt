package ren.natsuyuk1.comet.commands.service

import kotlinx.coroutines.delay
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.registerTimeout
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.objects.config.CometServerConfig
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.util.toMessageWrapper
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GithubCommandService {
    private val repoRegex = """(.*)/(.*)""".toRegex()

    class GitHubSubscribeSession(
        contact: PlatformCommandSender,
        val sender: PlatformCommandSender,
        user: CometUser,
        val owner: String,
        val name: String,
        private val groupID: Long
    ) : Session(contact, user) {
        override suspend fun process(message: MessageWrapper) {
            val raw = message.parseToString()
            val secret = if (raw == "完成订阅") {
                ""
            } else {
                raw
            }

            GitHubRepoData.data.repos.add(
                GitHubRepoData.Data.GithubRepo(
                    name,
                    owner,
                    secret,
                    mutableListOf(GitHubRepoData.Data.GithubRepo.GithubRepoSubscriber(groupID))
                )
            )

            sender.sendMessage("订阅仓库 $owner/$name 成功, 请至仓库 WebHook 设置 Comet 回调链接!".toMessageWrapper())

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
            val owner = slice[0]
            val name = slice[1]

            val repos = GitHubRepoData.data.repos

            val hasSubscribed = repos.any {
                val nameEq = it.getName() == repoName
                val groupEq = it.subscribers.any { sub -> sub.id == groupID }
                nameEq && groupEq
            }
            if (hasSubscribed) {
                subject.sendMessage("你已经订阅过这个仓库了!".toMessageWrapper())
                return
            }

            if (GitHubApi.isRepoExist(owner, name)) {
                val repo = GitHubRepoData.data.repos.find { it.getName() == "$owner/$name" }

                if (repo == null) {
                    if (subject is Group) {
                        subject.sendMessage("请在私聊中继续完成订阅".toMessageWrapper())
                    }

                    delay(1.seconds)

                    sender.sendMessage(
                        (
                            "你正在订阅仓库 $owner/$name, 是否需要添加仓库机密 (Secret)?\n" +
                                "添加机密可以保证传输仓库信息更加安全, 但千万别忘记了你设置的机密!\n" +
                                "如果无需添加, 请回复「完成订阅」, 反之直接发送你欲设置的机密."
                            ).toMessageWrapper()
                    )

                    GitHubSubscribeSession(subject, sender, user, owner, name, groupID).registerTimeout(1.minutes)
                } else {
                    repo.subscribers.add(GitHubRepoData.Data.GithubRepo.GithubRepoSubscriber(groupID))
                    if (CometServerConfig.data.serverName.isBlank()) {
                        subject.sendMessage("订阅仓库 $owner/$name 成功, 请至仓库 WebHook 设置添加 Comet 管理提供的链接!".toMessageWrapper())
                    } else {
                        subject.sendMessage(
                            """
                            订阅仓库 $owner/$name 成功, 请至仓库 WebHook 设置添加以下链接!
                            >> ${CometServerConfig.data.serverName}/github
                            """.trimIndent().toMessageWrapper()
                        )
                    }
                }
            } else {
                subject.sendMessage("找不到你想要订阅的 GitHub 仓库".toMessageWrapper())
            }
        } else {
            subject.sendMessage("请输入有效的 GitHub 仓库名称, 例如 StarWishsama/Comet-Bot".toMessageWrapper())
        }
    }

    suspend fun processUnsubscribe(
        subject: PlatformCommandSender,
        groupID: Long,
        repoName: String
    ) {
        if (repoName.matches(repoRegex)) {
            val slice = repoName.split("/")
            val owner = slice[0]
            val name = slice[1]

            val repos = GitHubRepoData.data.repos
            val repo = repos.find { it.getName() == "$owner/$name" }

            if (repo != null) {
                repo.subscribers.removeIf { it.id == groupID }
                subject.sendMessage("成功退订 $repoName!".toMessageWrapper())

                if (repo.subscribers.isEmpty()) {
                    repos.remove(repo)
                }
            } else {
                subject.sendMessage("找不到你想要取消订阅的 GitHub 仓库".toMessageWrapper())
            }
        } else {
            subject.sendMessage("请输入有效的 GitHub 仓库名称, 例如 StarWishsama/Comet-Bot".toMessageWrapper())
        }
    }

    // 3, 4
    private val githubLinkRegex by lazy { Regex("""^(https?://)?(www\.)?github\.com/(.+)/(.+)""") }

    suspend fun fetchRepoInfo(subject: PlatformCommandSender, repoName: String) {
        var owner: String? = null
        var name: String? = null

        if (repoRegex.matches(repoName)) {
            val split = repoName.split("/")

            owner = split[0]
            name = split[1]
        } else if (githubLinkRegex.matches(repoName)) {
            val groupVar = githubLinkRegex.find(repoName)?.groupValues

            if (groupVar.isNullOrEmpty() || groupVar.size < 4) {
                subject.sendMessage("请输入有效的仓库名/链接!".toMessageWrapper())
                return
            }

            owner = groupVar[3]
            name = groupVar[4]
        }

        if (owner == null || name == null) {
            subject.sendMessage("请输入有效的仓库名/链接!".toMessageWrapper())
            return
        }

        val image = GitHubApi.getRepoPreviewImage(owner, name)

        if (image == null) {
            subject.sendMessage("搜索不到这个仓库, 等会再试试吧~".toMessageWrapper())
        } else {
            subject.sendMessage(
                buildMessageWrapper {
                    appendElement(Image(url = image))
                    appendText("🔗 https://github.com/$owner/$name")
                }
            )
        }
    }

    suspend fun fetchSubscribeRepos(subject: PlatformCommandSender, groupID: Long) {
        val repos =
            GitHubRepoData.data.repos.filter { it.subscribers.any { g -> g.id == groupID } }

        if (repos.isEmpty()) {
            subject.sendMessage("本群还未订阅过任何仓库".toMessageWrapper())
        } else {
            subject.sendMessage(
                buildString {
                    append("本群已订阅仓库 >>")
                    appendLine()
                    repos.forEach { r ->
                        append(r.getName() + ", ")
                    }
                }.removeSuffix(", ").toMessageWrapper()
            )
        }
    }

    suspend fun fetchRepoSetting(subject: PlatformCommandSender, repoName: String, groupID: Long) {
        if (!repoRegex.matches(repoName)) {
            subject.sendMessage("请输入有效的 GitHub 仓库名称, 例如 StarWishsama/Comet-Bot".toMessageWrapper())
        } else {
            val repo =
                GitHubRepoData.data.repos.find {
                    val nameEq = it.getName() == repoName
                    val subEq = it.subscribers.any { g -> g.id == groupID }
                    nameEq && subEq
                }

            if (repo == null) {
                subject.sendMessage("找不到你想要查询设置的 GitHub 仓库".toMessageWrapper())
            } else {
                val subSetting = repo.subscribers.find { it.id == groupID } ?: kotlin.run {
                    subject.sendMessage("该群聊未订阅此 GitHub 仓库".toMessageWrapper())
                    return
                }

                subject.sendMessage(
                    buildMessageWrapper {
                        appendTextln("当前仓库 ${repo.getName()}")
                        appendTextln("订阅分支 >> ${subSetting.subscribeBranch}")
                        appendText("订阅事件 >> ${subSetting.subscribeEvent}")
                    }
                )
            }
        }
    }
}
