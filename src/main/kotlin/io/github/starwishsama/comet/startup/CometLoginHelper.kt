/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.startup

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.CometApplication
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import java.io.EOFException
import kotlin.system.exitProcess

class CometLoginHelper(val comet: Comet) {
    var status: LoginStatus = LoginStatus.WAITING

    fun solve() {
        while (status != LoginStatus.LOGGING) {
            try {
                if (comet.isInitialized()) {
                    BotVariables.daemonLogger.info("机器人已完成登录.")
                    break
                }

                var command: String

                if (comet.id == 0L || status == LoginStatus.INPUT_ID) {
                    BotVariables.daemonLogger.info("请输入欲登录的机器人账号. 如需退出, 请输入 stop")
                    command = CometApplication.console.readLine(">")

                    if (command == "stop") exitProcess(0)

                    if (command.isNumeric() && command.toLongOrNull() != null) {
                        comet.id = command.toLong()
                        BotVariables.daemonLogger.info("成功设置账号为 ${comet.id}")
                        status = LoginStatus.INPUT_PASSWORD
                    } else {
                        BotVariables.daemonLogger.info("请输入正确的 QQ 号!")
                    }
                } else if (comet.password.isEmpty() || status == LoginStatus.INPUT_PASSWORD || status == LoginStatus.LOGIN_FAILED) {
                    BotVariables.daemonLogger.info("请输入欲登录的机器人密码. 如需返回上一步, 请输入 back; 如需退出, 请输入 stop.")
                    command = CometApplication.console.readLine(">", '*')

                    if (command == "back") {
                        status = LoginStatus.INPUT_ID
                        continue
                    }

                    comet.password = command
                    BotVariables.daemonLogger.info("设置成功! 正在启动 Comet...")
                    status = LoginStatus.LOGGING
                    break
                } else if (comet.id > 0 && comet.password.isNotEmpty()) {
                    status = LoginStatus.LOGGING
                    break
                }
            } catch (e: EOFException) {
                break
            } catch (e: UserInterruptException) {
                break
            } catch (e: EndOfFileException) {
                break
            }
        }
    }
}

enum class LoginStatus {
    WAITING, INPUT_ID, INPUT_PASSWORD, LOGGING, LOGIN_FAILED, LOGIN_SUCCESS
}