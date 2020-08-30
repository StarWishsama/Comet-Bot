package io.github.starwishsama.bilibiliapi

import cn.hutool.http.HttpRequest
import com.google.gson.JsonParser
import io.github.starwishsama.bilibiliapi.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.bilibiliapi.data.dynamic.dynamicdata.UnknownType
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.MessageWrapper

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 * @author Nameless
 */
object MainApi : ApiExecutor {
    private var dynamicUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="
    private val agent = mutableMapOf("User-Agent" to "Nameless live status checker by StarWishsama")
    private const val apiRateLimit = "BiliBili API调用已达上限"

    @Throws(RateLimitException::class)
    fun getUserNameByMid(mid: Long): String {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        usedTime++
        val response = HttpRequest.get(infoUrl + mid).timeout(2000)
            .addHeaders(agent)
            .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["name"].asString
    }

    @Throws(RateLimitException::class)
    suspend fun getDynamic(mid: Long): MessageWrapper {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
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
                            MessageWrapper("错误: 不支持的动态类型")
                        }
                    }
                } catch (e: IllegalStateException) {
                    return MessageWrapper("没有发过动态")
                }
            }
        }
        return MessageWrapper("获取时出现问题")
    }

    override var usedTime: Int = 0

    override fun isReachLimit() : Boolean {
        return usedTime > getLimitTime()
    }

    override fun getLimitTime() : Int = 500
}