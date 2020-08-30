package io.github.starwishsama.bilibiliapi

import com.github.salomonbrys.kotson.fromJson
import io.github.starwishsama.bilibiliapi.data.login.GetKeyResponse
import io.github.starwishsama.bilibiliapi.data.login.LoginResponse
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.utils.network.NetUtil
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object LoginApi {
    private const val loginApi = "https://passport.bilibili.com/"
    lateinit var loginResponse: LoginResponse

    /**
     * 登陆
     * v3 登陆接口会同时返回 cookies 和 token
     * 如果要求验证码, 访问 data 中提供的 url 将打开一个弹窗, 里面会加载 js 并显示极验验证码
     * 极验会调用 https://api.geetest.com/ajax.php 上传滑动轨迹, 然后获得 validate 的值传入即可
     * secCode 的值应为 "$validate|jordan"
     *
     */
    fun login(
            username: String, password: String,
            // 如果登陆请求返回了 "验证码错误!"(-105) 的结果, 那么下一次发送登陆请求就需要带上验证码
            challenge: String? = null,
            secCode: String? = null,
            validate: String? = null
    ) {
        // 取得 hash 和 RSA 公钥
        val passportAPI = NetUtil.getPageContent("$loginApi/api/oauth2/getKey")
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

        loginResponse = gson.fromJson(doLogin)
    }
}