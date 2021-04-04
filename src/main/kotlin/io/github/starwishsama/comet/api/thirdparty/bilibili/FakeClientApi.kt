package io.github.starwishsama.comet.api.thirdparty.bilibili

import com.github.salomonbrys.kotson.get
import com.hiczp.bilibili.api.BilibiliClient
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.comet.BotVariables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                BotVariables.daemonLogger.info("成功登录哔哩哔哩账号")
            } catch (e: BilibiliApiException) {
                when (e.commonResponse.code) {
                    -629 -> {
                        BotVariables.daemonLogger.warning("哔哩哔哩账号密码错误, 请稍后在后台用 /bili login [账号] [密码] 重试!")
                    }
                    -105 -> {
                        BotVariables.daemonLogger.warning(
                            "极验滑块链接: ${e.commonResponse.data?.get("url")?.asString}" +
                                    "\n需要滑块验证码登录, 请稍后在后台用 /bili retry 进行进一步操作!\n" +
                                    "需要将获得的 challenge 和 validate 填入, 如果不懂如何获取请查看 Wiki"
                        )
                    }
                    else -> {
                        BotVariables.daemonLogger.warning("登录失败!", e)
                    }
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
        } catch (e: BilibiliApiException) {
            BotVariables.logger.warning(
                "在调用B站API时出现了问题, 响应码 ${e.commonResponse.code}\n" +
                        "${e.commonResponse.msg}\n" +
                        "${e.commonResponse.message}", e
            )
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