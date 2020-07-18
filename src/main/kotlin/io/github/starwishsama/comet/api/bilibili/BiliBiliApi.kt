package io.github.starwishsama.comet.api.bilibili

import cn.hutool.http.HttpRequest
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata.UnknownType

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 * @author Nameless
 */
object BiliBiliApi : ApiExecutor {
    private var dynamicUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="
    private const val liveUrl = "http://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid="
    private val agent = mutableMapOf("User-Agent" to "Nameless live status checker by StarWishsama")

    @Throws(RateLimitException::class)
    fun getUserNameByMid(mid: Long): String {
        if (isReachLimit()) {
            throw RateLimitException("BiliBili API调用已达上限")
        }

        usedTime++
        val response = HttpRequest.get(infoUrl + mid).timeout(2000)
            .addHeaders(agent)
            .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["name"].asString
    }

    @Throws(RateLimitException::class)
    fun getLiveStatus(mid: Long): Boolean {
        if (isReachLimit()) {
            throw RateLimitException("BiliBili API调用已达上限")
        }

        usedTime++
        val response = HttpRequest.get(liveUrl + mid).timeout(2000)
            .addHeaders(agent)
            .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["liveStatus"].asInt == 1
    }

    @Throws(RateLimitException::class)
    fun getRoomIdByMid(mid: Long): Long {
        if (isReachLimit()) {
            throw RateLimitException("BiliBili API调用已达上限")
        }

        usedTime++
        val response = HttpRequest.get(liveUrl + mid).timeout(2000)
            .addHeaders(agent)
            .executeAsync()
        return JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["roomid"].asLong
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