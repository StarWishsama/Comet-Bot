package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper

object GroupConfigService {
    /**
     * 设置群助手
     *
     * @param cfg 群配置 [PerGroupConfig]
     * @param helperId 助手 ID
     *
     * @return 结果文本
     */
    fun setHelper(cfg: PerGroupConfig, helperId: Long): MessageWrapper {
        return if (cfg.isHelper(helperId)) {
            cfg.removeHelper(helperId)
            MessageWrapper().addText("成功将 $helperId 移出群助手列表")
        } else {
            cfg.addHelper(helperId)
            MessageWrapper().addText("成功将 $helperId 加入群助手列表")
        }
    }

    /**
     * 设置是否可以在群里复读
     *
     * @param cfg 群配置 [PerGroupConfig]
     *
     * @return 是否可以复读
     */
    fun setRepeat(cfg: PerGroupConfig): Boolean {
        cfg.canRepeat = !cfg.canRepeat
        return cfg.canRepeat
    }

    fun setAutoAcceptRequest(cfg: PerGroupConfig, switch: Boolean, condition: String = ""): MessageWrapper {
        return if (switch) {
            cfg.autoAccept = true

            if (condition.isBlank()) {
                buildMessageWrapper {
                    addText("已开启自动接受加群请求")
                }
            } else {
                cfg.autoAcceptCondition = condition
                MessageWrapper().addText("已开启自动接受加群请求, 条件为：$condition")
            }
        } else {
            cfg.autoAccept = false
            MessageWrapper().addText("成功关闭自动接收加群请求")
        }
    }

    fun disableCommand(cfg: PerGroupConfig, commandName: String): MessageWrapper {
        val command = CommandManager.getCommand(commandName)

        return MessageWrapper().addText(command?.props?.disableCommand(cfg.id)?.msg ?: "找不到对应命令")
    }

    fun setNewComerMessage(cfg: PerGroupConfig, welcomeText: MessageWrapper): MessageWrapper {
        return if (welcomeText.parseToString().isBlank()) {
            cfg.newComerWelcome = !cfg.newComerWelcome
            MessageWrapper().addText("已${if (cfg.newComerWelcome) "开启" else "关闭"}加群自动欢迎")
        } else {
            cfg.newComerWelcomeText = welcomeText
            MessageWrapper().addText("设置欢迎消息成功")
        }
    }

    fun setFileRemovePattern(cfg: PerGroupConfig, pattern: String): MessageWrapper {
        return if (pattern.isNotBlank()) {
            cfg.oldFileMatchPattern = pattern
            buildMessageWrapper { addText("已设置文件匹配正则表达式为 [${cfg.oldFileMatchPattern}]") }
        } else {
            buildMessageWrapper { addText("请输入正确的正则表达式") }
        }
    }

    fun setFileRemoveDelay(cfg: PerGroupConfig, delay: Long): MessageWrapper {
        cfg.oldFileCleanDelay = delay
        return buildMessageWrapper { addText("已设置自动删除超过 $delay ms 的文件.") }
    }


    fun setFileRemove(cfg: PerGroupConfig): MessageWrapper {
        cfg.oldFileCleanFeature = !cfg.oldFileCleanFeature
        return MessageWrapper().addText("已${if (cfg.oldFileCleanFeature) "开启" else "关闭"}群文件自动删除功能")
    }

}