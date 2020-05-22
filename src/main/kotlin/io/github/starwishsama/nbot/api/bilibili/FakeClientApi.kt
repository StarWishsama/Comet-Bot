package io.github.starwishsama.nbot.api.bilibili

import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.live.model.RoomInfo
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.nbot.BotInstance

object FakeClientApi {
    private val client = BotInstance.client

    private suspend fun searchUser(userName: String): SearchUserResult.Data {
        val searchResult = client.appAPI.searchUser(keyword = userName).await()
        return searchResult.data
    }

    suspend fun getLiveRoom(roomId: Long): RoomInfo? {
        try {
            return client.liveAPI.getInfo(roomId).await()
        } catch (e: BilibiliApiException) {
            BotInstance.logger.error("在调用B站API时出现了问题, 响应码 ${e.commonResponse.code}\n" +
                    "${e.commonResponse.msg}\n" +
                    "${e.commonResponse.message}", e)
        }
        return null
    }

    suspend fun getUser(userName: String): SearchUserResult.Data.Item? {
        try {
            val searchResult =
                searchUser(userName)
            if (!searchResult.items.isNullOrEmpty()) {
                return searchResult.items[0]
            }
        } catch (e: BilibiliApiException) {
            BotInstance.logger.error("在调用B站API时出现了问题, 响应码 ${e.commonResponse.code}\n" +
                    "${e.commonResponse.msg}\n" +
                    "${e.commonResponse.message}", e)
        }
        return null
    }
}