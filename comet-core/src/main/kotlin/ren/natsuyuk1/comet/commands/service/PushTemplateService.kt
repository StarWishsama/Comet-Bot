package ren.natsuyuk1.comet.commands.service

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.event.registerListener
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.event.pusher.pushtemplate.PushTemplateReceiveEvent
import ren.natsuyuk1.comet.objects.config.PushTemplate
import ren.natsuyuk1.comet.objects.config.PushTemplateConfig
import ren.natsuyuk1.comet.pusher.toCometPushTarget
import ren.natsuyuk1.comet.util.toMessageWrapper
import java.util.*

enum class PushTemplateSubscribeStage {
    TEMPLATE, URL
}

class PushTemplateNewSession(
    private val templateName: String,
    private val requestGroup: Group,
    contact: PlatformCommandSender,
    user: CometUser,
) : Session(contact, user) {
    var stage = PushTemplateSubscribeStage.TEMPLATE
    var template = ""
    var url = ""

    override suspend fun process(message: MessageWrapper) {
        when (stage) {
            PushTemplateSubscribeStage.TEMPLATE -> {
                template = message.parseToString()
                contact.sendMessage("成功设置模板, 接下来请回复我推送服务器的地址".toMessageWrapper())
            }

            PushTemplateSubscribeStage.URL -> {
                url = message.parseToString()
                val token = UUID.randomUUID()
                PushTemplateConfig.data.add(
                    PushTemplate(
                        templateName,
                        template,
                        mutableListOf(requestGroup.toCometPushTarget()),
                        url,
                        token
                    )
                )
                contact.sendMessage(
                    buildMessageWrapper {
                        appendTextln("成功新建推送模板 $templateName! 已自动订阅你发起新建请求的群聊.")
                        appendTextln("你的推送 Token 是 $token")
                        appendText("该 Token 之后无法再获取, 请记好你的 Token.")
                    }
                )
                expire()
            }
        }
    }
}

object PushTemplateService {
    fun new(group: Group, sender: PlatformCommandSender, user: CometUser, templateName: String): MessageWrapper {
        if (PushTemplateConfig.data.any { it.templateName == templateName }) {
            return "已存在相同的推送模板 $templateName".toMessageWrapper()
        }

        PushTemplateNewSession(templateName, group, sender, user).register()
        return "请添加机器人为好友, 并在私聊中继续完成新建模板".toMessageWrapper()
    }

    fun remove(templateName: String): MessageWrapper {
        return if (PushTemplateConfig.data.removeIf { it.templateName == templateName }) {
            "成功删除模板 $templateName".toMessageWrapper()
        } else {
            "找不到推送模板 $templateName".toMessageWrapper()
        }
    }

    fun subscribe(templateName: String, group: Group): MessageWrapper {
        val pt = PushTemplateConfig.data.find { it.templateName == templateName }

        return if (pt == null) {
            "找不到模板 $templateName".toMessageWrapper()
        } else {
            pt.subscribers.add(group.toCometPushTarget())
            "订阅模板成功".toMessageWrapper()
        }
    }

    fun unsubscribe(templateName: String, group: Group): MessageWrapper {
        val pt = PushTemplateConfig.data.find { it.templateName == templateName }

        return if (pt == null) {
            "找不到模板 $templateName".toMessageWrapper()
        } else {
            pt.subscribers.removeIf { it.id == group.id && it.platform == group.platform }
            "退订模板成功".toMessageWrapper()
        }
    }

    fun list(group: Group): MessageWrapper {
        val pts = PushTemplateConfig.data.filter { pt ->
            pt.subscribers.any { it.id == group.id && it.platform == group.platform }
        }

        return buildMessageWrapper {
            appendTextln("已订阅的推送模板 >>")
            pts.forEachIndexed { i, pt ->
                val content = "[$i] ${pt.templateName}"
                if (i != pts.size - 1) {
                    appendTextln(content)
                } else {
                    appendText(content)
                }
            }
        }
    }
}

/**
 * 快速为一个 [Comet] 实例监听推送模板事件
 */
fun Comet.subscribePushTemplateEvent() = run {
    registerListener<PushTemplateReceiveEvent> { event ->
        logger.debug { "Processing PushTemplateReceiveEvent: $event" }

        event.broadcastTargets.forEach {
            val target = getGroup(it.id) ?: return@forEach

            target.sendMessage(event.content)

            logger.debug { "已推送来自 ${event.pushTemplate.url} 的事件至群 ${it.id}" }
        }
    }
}
