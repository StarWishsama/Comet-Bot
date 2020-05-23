package io.github.starwishsama.nbot.api.bilibili

import cn.hutool.http.HttpRequest
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.BotConstants.gson
import io.github.starwishsama.nbot.api.ApiExecutor
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicTypeSelector
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata.UnknownType
import java.time.LocalDateTime

/**
 * BiliBili 动态 API
 * 获取用户的最新动态
 * 支持多种格式
 * @author Nameless
 */
object DynamicApi : ApiExecutor {
    private var dynamicUrl =
            "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="

    @Throws(RateLimitException::class)
    fun getUserNameByMid(mid: Long): String {
        if (isReachLimit()) {
            throw RateLimitException("BiliBili API调用已达上限")
        }

        usedTime++
        val response = HttpRequest.get(infoUrl + mid).timeout(8000)
                .addHeaders(mutableMapOf("User-Agent" to "Nameless live status checker by StarWishsama"))
                .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["name"].asString
    }

    @Throws(RateLimitException::class)
    suspend fun getDynamic(mid: Long): WrappedMessage {
        if (isReachLimit()) {
            throw RateLimitException("BiliBili API调用已达上限")
        }
        usedTime++
        val response = HttpRequest.get(dynamicUrl.replace("%uid%", mid.toString())).executeAsync()
        if (response.isOk) {
            val dynamicObject = JsonParser.parseString(response.body())
            if (dynamicObject.isJsonObject) {
                try {
                    val entity = dynamicObject.asJsonObject["data"].asJsonObject["cards"].asJsonArray[0]
                    val dynamicInfo = entity.asJsonObject["card"].asString
                    val singleDynamicObject = JsonParser.parseString(dynamicInfo)
                    if (singleDynamicObject.isJsonObject) {
                        val dynamicType = DynamicTypeSelector.getType(entity.asJsonObject["desc"].asJsonObject["type"].asInt)
                        return if (dynamicType.typeName != UnknownType::javaClass.name) {
                            gson.fromJson(dynamicInfo, dynamicType).getContact()
                        } else {
                            WrappedMessage("错误: 不支持的动态类型")
                        }
                    }
                } catch (e: IllegalStateException) {
                    return WrappedMessage("没有发过动态")
                }
            }
        }
        return WrappedMessage("获取时出问题")
    }

    override var usedTime: Int = 0

    override fun isReachLimit() : Boolean {
        return usedTime > getLimitTime()
    }

    override fun getLimitTime() : Int = 500
}