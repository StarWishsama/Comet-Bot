/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.login.GetKeyResponse
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.login.LoginResponse
import io.github.starwishsama.comet.utils.network.NetUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object LoginApi {

    private const val loginApiEndpoint = "https://passport.bilibili.com/"
    var loginResponse: LoginResponse? = null

    val loginApiService: ILoginApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(loginApiEndpoint)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(BotVariables.client)
            .build()
        loginApiService = retrofit.create(ILoginApi::class.java)
    }


    interface ILoginApi {
        @GET("api/v3/oauth2/login")
        fun login(
            @Query("captcha", encoded = true)
            captcha: Int = 6,
            @Query("username", encoded = true)
            username: String,
            @Query("password", encoded = true)
            password: String,
            // 如果登陆请求返回了 "验证码错误!"(-105) 的结果, 那么下一次发送登陆请求就需要带上验证码
            @Query("challenge", encoded = true)
            challenge: String? = null,
            @Query("secCode", encoded = true)
            secCode: String? = null,
            @Query("validate", encoded = true)
            validate: String? = null,
            @Query("keep", encoded = true)
            keep: Boolean = true
        ): Call<LoginResponse>
    }

    /**
     * 登陆
     * v3 登陆接口会同时返回 cookies 和 token
     * 如果要求验证码, 访问 [LoginResponse.data] 中提供的 [LoginResponse.Data.url] 将打开一个弹窗, 里面会加载 js 并显示极验验证码
     * 极验会调用 https://api.geetest.com/ajax.php 上传滑动轨迹, 然后获得 validate 的值传入即可
     * secCode 的值应为 "$validate|jordan"
     *
     * 注意: 加密盐值有效时间为 20s
     *
     * 登录结果请查看 [LoginApi.loginResponse]
     */
    fun login(
        username: String, password: String,
        // 如果登陆请求返回了 "验证码错误!"(-105) 的结果, 那么下一次发送登陆请求就需要带上验证码
        challenge: String? = null,
        secCode: String? = null,
        validate: String? = null
    ) {
        // 取得 hash 和 RSA 公钥
        val keyResponse = getKey()

        if (keyResponse == null) {
            daemonLogger.info("登入哔哩哔哩账号失败! 无法获取密钥")
            return
        }

        val (hash, key) = keyResponse.data.let { data ->
            data.hash to data.key.split('\n').filterNot { it.startsWith('-') }.joinToString(separator = "")
        }

        // 解析 RSA 公钥
        val publicKey = X509EncodedKeySpec(Base64.getDecoder().decode(key)).let {
            KeyFactory.getInstance("RSA").generatePublic(it)
        }

        // 加密密码
        val cipheredPassword = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }.doFinal((hash + password).toByteArray()).let {
            Base64.getEncoder().encode(it)
        }.let {
            String(it)
        }
        val response = loginApiService.login(
            username = username,
            password = cipheredPassword,
            challenge = challenge,
            secCode = secCode,
            validate = validate
        ).execute().body()

        if (response != null && response.code != -105) {
            this.loginResponse = response
        } else {
            if (response == null) {
                daemonLogger.warning("登入哔哩哔哩账号失败, 登录响应异常")
                return
            }

            daemonLogger.info("登入哔哩哔哩账号失败! 错误码 ${response.code}, 错误信息 ${response.message}")
            if (response.data.url != null) {
                daemonLogger.info("GeeTest url is ${response.data.url}, after verified successfully, please use validate to relogin")
            }
        }
    }

    /**
     * 获取 B 站登录所需的密钥
     */
    private fun getKey(): GetKeyResponse? {
        val keyResponse = NetUtil.getPageContent("${loginApiEndpoint}/login?act=getkey")
        return mapper.readValue(keyResponse ?: return null)
    }

    fun getToken(): String = loginResponse?.token ?: throw RuntimeException("需要先登入哔哩哔哩账号才能使用!")
}