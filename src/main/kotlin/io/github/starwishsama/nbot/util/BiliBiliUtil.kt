package io.github.starwishsama.nbot.util

import cn.hutool.http.HttpRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.live.model.RoomInfo
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicAdapter
import io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata.UnknownType

object BiliBiliUtil {
    private val client = BotInstance.client
    private val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    private var dynamicUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="

    suspend fun searchUser(userName: String): SearchUserResult.Data {
        val searchResult = client.appAPI.searchUser(keyword = userName).await()
        return searchResult.data
    }

    suspend fun getLiveRoom(roomId: Long): RoomInfo {
        return client.liveAPI.getInfo(roomId).await()
    }

    fun getUserNameByMid(mid: Long): String {
        val response = HttpRequest.get(infoUrl + mid).timeout(8000)
            .addHeaders(mutableMapOf("User-Agent" to "Nameless live status checker (starwishsama@outlook.com)"))
            .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["name"].asString
    }

    suspend fun getDynamic(mid: Long): List<String> {
        val response = HttpRequest.get(dynamicUrl.replace("%uid%", mid.toString())).executeAsync()
        if (response.isOk) {
            val dynamicObject = JsonParser.parseString(response.body())
            if (dynamicObject.isJsonObject) {
                try {
                    val entity = dynamicObject.asJsonObject["data"].asJsonObject["cards"].asJsonArray[0]
                    val dynamicInfo = entity.asJsonObject["card"].asString
                    val trueDynamicJson = JsonParser.parseString(dynamicInfo)
                    if (trueDynamicJson.isJsonObject) {
                        val dynamicType = DynamicAdapter.getType(entity.asJsonObject["desc"].asJsonObject["type"].asInt)
                        return if (dynamicType.typeName != UnknownType::javaClass.name) {
                            val info = gson.fromJson(dynamicInfo, dynamicType)
                            info.getContact()
                        } else {
                            arrayListOf("错误: 不支持的动态类型")
                        }
                    }
                } catch (e: IllegalStateException){
                    return arrayListOf("没有发过动态")
                }
            }
        }
        return arrayListOf("获取时出问题")
    }
}