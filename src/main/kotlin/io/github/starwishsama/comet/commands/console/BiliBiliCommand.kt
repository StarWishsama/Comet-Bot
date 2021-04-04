package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi.cacheLoginData
import io.github.starwishsama.comet.enums.UserLevel

class BiliBiliCommand : ConsoleCommand {
    override suspend fun execute(args: List<String>): String {
        if (args.isEmpty()) {
            return getHelp()
        }

        when (args[0].toLowerCase()) {
            "login" -> {
                FakeClientApi.login(cacheLoginData.userName, cacheLoginData.passWord)
            }

            "retry" -> {
                if (cacheLoginData.isEmpty()) {
                    return "你还未登录过哔哩哔哩账号! 请使用 /bili login 先登录."
                }

                if (args.size > 1) {
                    BotVariables.daemonLogger.info("challenge = ${args[1]}, validate = ${args[2]}")

                    FakeClientApi.login(
                        cacheLoginData.userName,
                        cacheLoginData.passWord,
                        challenge = args[1],
                        secCode = "${args[2]}|jordan",
                        validate = args[2]
                    )
                } else {
                    return getHelp()
                }
            }
        }

        return getHelp()
    }

    override fun getProps(): CommandProps {
        return CommandProps(
            "bili",
            arrayListOf("bl", "哔哩哔哩"),
            "登录哔哩哔哩账号",
            "",
            UserLevel.CONSOLE
        )
    }

    override fun getHelp(): String {
        return """
            /bili login [手机号/邮箱/UID] [密码] 登录哔哩哔哩
            /bili retry [challenge] [validate] 通过极验验证码重试登录
        """.trimIndent()
    }
}