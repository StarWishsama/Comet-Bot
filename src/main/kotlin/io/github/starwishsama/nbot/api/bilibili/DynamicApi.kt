package io.github.starwishsama.nbot.api.bilibili

import cn.hutool.http.HttpRequest
import com.google.gson.JsonParser
import com.hiczp.bilibili.api.app.model.SearchUserResult
import com.hiczp.bilibili.api.live.model.RoomInfo
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException
import io.github.starwishsama.nbot.BotConstants.gson
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.api.ApiExecutor
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicTypeSelector
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata.UnknownType
import java.time.LocalDateTime

object DynamicApi : ApiExecutor {
    private var dynamicUrl =
            "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="

    fun getUserNameByMid(mid: Long): String {
        val response = HttpRequest.get(infoUrl + mid).timeout(8000)
                .addHeaders(mutableMapOf("User-Agent" to "Nameless live status checker by StarWishsama"))
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
                        val dynamicType = DynamicTypeSelector.getType(entity.asJsonObject["desc"].asJsonObject["type"].asInt)
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

    override val usedTime: Int
        get() = TODO("Not yet implemented")
    override val lastUsedTime: LocalDateTime
        get() = TODO("Not yet implemented")

    override fun isReachLimit() {
        TODO("Not yet implemented")
    }

    override fun getLimitTime() {
        TODO("Not yet implemented")
    }
}