package io.github.starwishsama.nbot.util

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.live.model.RoomInfo
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.objects.BotLocalization
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicAdapter
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

object BiliBiliUtil {
    private val client = BotInstance.client
    private val gson : Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    private var dynamicUrl = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"

    suspend fun searchUser(userName: String): SearchUserResult.Data {
        val searchResult = client.appAPI.searchUser(keyword = userName).await()
        return searchResult.data
    }

    suspend fun getLiveRoom(roomId: Long): RoomInfo {
        return client.liveAPI.getInfo(roomId).await()
    }

    suspend fun getDynamic(mid: Long, contact: Contact): MessageChain {
        val response = HttpRequest.get(dynamicUrl.replace("%uid%", mid.toString())).executeAsync()
        if (response.isOk) {
            val dynamicObject = JsonParser.parseString(response.body())
            if (dynamicObject.isJsonObject){
                val entity = dynamicObject.asJsonObject["data"].asJsonObject["cards"].asJsonArray[0]
                val dynamicInfo = entity.asJsonObject["card"].asString
                val trueDynamicJson= JsonParser.parseString(dynamicInfo)
                if (trueDynamicJson.isJsonObject){
                    val type = entity.asJsonObject["desc"].asJsonObject["type"].asInt
                    val result = gson.fromJson(dynamicInfo, DynamicAdapter.getType(type))
                    return result.getMessageChain(contact)
                }
            }
        }
        return "获取时出问题".toMessage().asMessageChain()
    }
}