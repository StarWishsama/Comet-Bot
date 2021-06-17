/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import com.hiczp.bilibili.api.BilibiliClient
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.logger.HinaLogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 调用 [BilibiliClient] 的辅助 API 类
 *
 */
object FakeClientApi {
    val client = BilibiliClient()
    var cacheLoginData: LoginData = LoginData("", "")

    suspend fun login(userName: String, password: String) {
        cacheLoginData.cache(userName, password)
        login(userName, password, null, null, null)
    }

    suspend fun login(userName: String, password: String, challenge: String?, secCode: String?, validate: String?) {
        cacheLoginData.cache(userName, password, challenge, validate)
        withContext(Dispatchers.IO) {
            try {
                client.login(userName, password, challenge, secCode, validate)
                BotVariables.daemonLogger.info("哔哩哔哩账号登录详情: ${client.loginResponse}")
            } catch (e: IOException) {
                if (e is BilibiliApiException) {
                    BotVariables.daemonLogger.log(HinaLogLevel.Debug, "", e, bypass = true)
                    when (e.commonResponse.code) {
                        -629 -> {
                            BotVariables.daemonLogger.warning("哔哩哔哩账号密码错误, 请稍后在后台用 /bili login [账号] [密码] 重试!")
                        }
                        -105 -> {
                            BotVariables.daemonLogger.warning(
                                "极验滑块链接: ${e.commonResponse.data?.asJsonObject?.get("url")?.asString}" +
                                        "\n需要滑块验证码登录, 请稍后在后台用 /bili retry 进行进一步操作!\n" +
                                        "需要将获得的 challenge 和 validate 填入, 如果不懂如何获取请查看 Wiki"
                            )
                        }
                        else -> {
                            BotVariables.daemonLogger.warning("登录失败! 响应码为 ${e.commonResponse.code}", e)
                        }
                    }
                } else {
                    BotVariables.daemonLogger.warning("登录失败!", e)
                }
            }
        }
    }

    private suspend fun searchUser(userName: String): SearchUserResult.Data {
        val searchResult = client.appAPI.searchUser(keyword = userName).await()
        return searchResult.data
    }

    suspend fun getUser(userName: String): SearchUserResult.Data.Item? {
        try {
            val searchResult =
                searchUser(userName)
            if (!searchResult.items.isNullOrEmpty()) {
                return searchResult.items[0]
            }
        } catch (e: IOException) {
            if (e is BilibiliApiException) {
                BotVariables.logger.warning(
                    "在调用B站API时出现了问题, 响应码 ${e.commonResponse.code}\n" +
                            "${e.commonResponse.msg}\n" +
                            "${e.commonResponse.message}", e
                )
            } else {
                BotVariables.logger.warning("在搜索B站用户时出现了意外", e)
            }
        }
        return null
    }
}

data class LoginData(
    var userName: String,
    var passWord: String,
    var challenge: String? = null,
    var validate: String? = null
) {
    fun cache(userName: String, password: String, challenge: String? = null, validate: String? = null) {
        this.userName = userName
        this.passWord = password
        this.challenge = challenge
        this.validate = validate
    }

    fun isEmpty(): Boolean = userName.isEmpty() || passWord.isEmpty()
}