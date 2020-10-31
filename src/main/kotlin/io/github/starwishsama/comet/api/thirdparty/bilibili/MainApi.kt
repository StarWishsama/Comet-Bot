package io.github.starwishsama.comet.api.thirdparty.bilibili

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.UnknownType
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

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
        val response = HttpRequest.get(dynamicUrl.replace("%uid%", mid.toString())).executeAsync()
        if (response.isOk) {
            val dynamicObject = JsonParser.parseString(response.body())
            if (dynamicObject.isJsonObject) {
                try {
                    val card = gson.fromJson<Dynamic>(dynamicObject.asJsonObject["data"].asJsonObject["cards"].asJsonArray[0])
                    val singleDynamicObject = JsonParser.parseString(card.card)
                    if (singleDynamicObject.isJsonObject) {
                        val dynamicType = DynamicTypeSelector.getType(card.description.type)
                        return if (dynamicType != UnknownType::class) {
                            gson.fromJson(card.card, dynamicType).getContact()
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

    /**
     * 搜索用户
     *
     * 上方搜索栏 -> 用户
     *
     * @param order 排序维度. totalrank 默认排序,fans 粉丝, level 等级.
     * @param orderSort 排序顺序. 0 从高到低, 1 从低到高.
     * @param userType 用户类型. 0 全部用户, 1 up主, 2 普通用户, 3 认证用户.
     *
     */
    /**@Suppress("SpellCheckingInspection")
    fun searchUser(
    @Query("highlight") highlight: Int = 1,
    @Query("keyword") keyword: String,
    @Query("order") order: String = "totalrank",
    @Query("order_sort") orderSort: Int? = null,
    @Query("pn") pageNumber: Int = 1,
    @Query("ps") pageSize: Int = 20,
    @Query("type") type: Int = 2,
    @Query("user_type") userType: Int = 0
    ): SearchUserResult {
    val requestUrl = "https://app.bilibili.com/x/v2/search/type"
    }*/

    override var usedTime: Int = 0

    override fun isReachLimit(): Boolean {
        val result = usedTime > getLimitTime()
        if (!result) usedTime++
        return result
    }

    override fun getLimitTime(): Int = 500
}