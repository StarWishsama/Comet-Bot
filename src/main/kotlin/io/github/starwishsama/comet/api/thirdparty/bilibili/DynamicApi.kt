package io.github.starwishsama.comet.api.thirdparty.bilibili

import cn.hutool.http.HttpRequest
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.UnknownType
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.json.isUsable
import io.github.starwishsama.comet.utils.network.NetUtil
import java.io.IOException

/**
 * BiliBili 动态 API
 *
 * 获取用户的最新动态
 * 支持多种格式
 * @author Nameless
 */
object DynamicApi : ApiExecutor {
    private const val dynamicUrl =
            "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=%uid%&offset_dynamic_id=0&need_top=0"
    private const val infoUrl = "http://api.bilibili.com/x/space/acc/info?mid="
    private const val dynamicByIdUrl =
            "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id="
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
        return mapper.readTree(response.body())["data"]["name"].asText()
    }

    @Throws(ApiException::class)
    fun getDynamicById(id: Long): Dynamic {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        NetUtil.executeHttpRequest(
                url = dynamicByIdUrl + id.toString()
        ).use { res ->
            if (res.isSuccessful) {
                val body = res.body?.string() ?: throw ApiException("无法获取动态页面")
                return mapper.readValue(body)
            } else {
                throw ApiException("无法获取动态页面, 状态码 ${res.code}")
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    fun getUserDynamicTimeline(mid: Long): Dynamic? {
        if (isReachLimit()) {
            throw RateLimitException(apiRateLimit)
        }

        NetUtil.executeHttpRequest(
                url = dynamicUrl.replace("%uid%", mid.toString())
        ).use { response ->
            val url = response.request.url.toString()
            var body = ""
            try {
                if (response.isSuccessful) {
                    body = response.body?.string() ?: return null
                    return mapper.readValue(body)
                }
            } catch (e: Exception) {
                if (e is JsonSyntaxException || e is JsonParseException || e !is IOException) {
                    FileUtil.createErrorReportFile("解析动态失败", "bilibili", e, body, "请求 URL 为: $url")
                    throw ApiException("解析动态失败")
                } else {
                    daemonLogger.warning("解析动态时出现异常", e)
                }
            }
        }
        throw ApiException("无法获取动态")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(ApiException::class)
    fun getWrappedDynamicTimeline(mid: Long): MessageWrapper {
        val dynamic = getUserDynamicTimeline(mid) ?: return MessageWrapper().addText("无法获取动态").setUsable(false)

        try {
            if (dynamic.data.cards != null) {
                if (dynamic.data.cards.isEmpty()) return MessageWrapper().addText("没有发过动态").setUsable(false)

                val card = dynamic.data.cards[0]
                val singleDynamicObject = mapper.readTree(card.card)
                if (singleDynamicObject.isUsable()) {
                    val dynamicType = DynamicTypeSelector.getType(card.description.type)
                    return if (dynamicType != UnknownType::class) {
                        mapper.readValue(card.card, dynamicType).getContact()
                    } else {
                        MessageWrapper().addText("错误: 不支持的动态类型").setUsable(false)
                    }
                }
            }
        } catch (e: Exception) {
            return MessageWrapper().addText("解析动态失败").setUsable(false)
        }

        return MessageWrapper().addText("获取动态失败").setUsable(false)
    }

    /**
     * 搜索用户
     *
     * 上方搜索栏 -> 用户
     *
     *  order 排序维度. totalrank 默认排序,fans 粉丝, level 等级.
     *  orderSort 排序顺序. 0 从高到低, 1 从低到高.
     *  userType 用户类型. 0 全部用户, 1 up主, 2 普通用户, 3 认证用户.
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
    override val duration: Int = 3

    override fun isReachLimit(): Boolean {
        val result = usedTime > getLimitTime()
        if (!result) usedTime++
        return result
    }

    override fun getLimitTime(): Int = 500
}