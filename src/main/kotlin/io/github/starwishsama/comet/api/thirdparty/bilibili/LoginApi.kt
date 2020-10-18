package io.github.starwishsama.comet.api.thirdparty.bilibili

import com.github.salomonbrys.kotson.fromJson
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.login.GetKeyResponse
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.login.LoginResponse
import io.github.starwishsama.comet.utils.network.NetUtil
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object LoginApi {
    private const val loginApi = "https://passport.bilibili.com/"
    var loginResponse: LoginResponse? = null

    /**
     * 登陆
     * v3 登陆接口会同时返回 cookies 和 token
     * 如果要求验证码, 访问 [LoginResponse.data] 中提供的 [LoginResponse.Data.url] 将打开一个弹窗, 里面会加载 js 并显示极验验证码
     * 极验会调用 https://api.geetest.com/ajax.php 上传滑动轨迹, 然后获得 validate 的值传入即可
     * secCode 的值应为 "$validate|jordan"
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
        val path = "/api/oauth2/getKey"
        // 取得 hash 和 RSA 公钥
        val passportAPI = NetUtil.getPageContent("$loginApi$path")
        val keyResponse = gson.fromJson<GetKeyResponse>(passportAPI)

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

        val doLogin = NetUtil.getPageContent("$loginApi/api/v3/oauth2/login?username=$username&password=$cipheredPassword&challenge=$challenge&seccode=$secCode&validate=$validate")
        val response = gson.fromJson<LoginResponse>(doLogin)

        if (response.code != -105) {
            loginResponse = response
        } else {
            daemonLogger.info("登入哔哩哔哩账号失败! 错误码 ${response.code}, 错误信息 ${response.message}")
            if (response.data.url != null) {
                daemonLogger.info("GeeTest url is ${response.data.url}, after verified, please use validate to relogin")
            }
        }
    }

    fun getToken(): String = loginResponse?.token ?: throw RuntimeException("需要先登入哔哩哔哩账号才能使用!")

}