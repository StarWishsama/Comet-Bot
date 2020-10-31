package io.github.starwishsama.comet.api.thirdparty.bilibili

import com.hiczp.bilibili.api.BilibiliClient
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.comet.BotVariables

/**
 * 调用 [BilibiliClient] 的辅助 API 类
 *
 */
object FakeClientApi {
    val client = BilibiliClient()

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
            BotVariables.logger.error(
                    "在调用B站API时出现了问题, 响应码 ${e.commonResponse.code}\n" +
                            "${e.commonResponse.msg}\n" +
                            "${e.commonResponse.message}", e
            )
        }
        return null
    }
}