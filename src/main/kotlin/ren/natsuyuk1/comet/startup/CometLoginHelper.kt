/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.startup

import io.github.starwishsama.comet.CometApplication
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import java.io.EOFException
import kotlin.system.exitProcess

class CometLoginHelper(val comet: ren.natsuyuk1.comet.Comet) {
    var status: LoginStatus = LoginStatus.WAITING

    fun solve() {
        while (status != LoginStatus.LOGGING) {
            try {
                if (comet.isInitialized()) {
                    CometVariables.daemonLogger.info("机器人已完成登录.")
                    break
                }

                if (comet.id == 0L || status == LoginStatus.INPUT_ID) {
                    CometVariables.daemonLogger.info("请输入欲登录的机器人账号. 如需退出, 请输入 stop")
                    val inputAccount = CometApplication.console.readLine(">")

                    if (inputAccount == "stop") exitProcess(0)

                    if (inputAccount.isNumeric() && inputAccount.toLongOrNull() != null) {
                        comet.id = inputAccount.toLong()
                        CometVariables.daemonLogger.info("成功设置账号为 ${comet.id}")
                        status = LoginStatus.INPUT_PASSWORD
                    } else {
                        CometVariables.daemonLogger.info("请输入正确的 QQ 号!")
                    }
                } else if (comet.password.isEmpty() || status == LoginStatus.INPUT_PASSWORD || status == LoginStatus.LOGIN_FAILED) {
                    CometVariables.daemonLogger.info("请输入欲登录的机器人密码. 如需返回上一步, 请输入 back; 如需退出, 请输入 stop.")
                    val inputPassword = CometApplication.console.readLine(">", '*')

                    if (inputPassword == "back") {
                        status = LoginStatus.INPUT_ID
                        continue
                    }

                    if (inputPassword == "stop") exitProcess(0)

                    comet.password = inputPassword
                    CometVariables.daemonLogger.info("设置成功! 正在启动 Comet...")
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