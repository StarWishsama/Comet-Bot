/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili.data.login

import com.fasterxml.jackson.annotation.JsonProperty

// From https://github.com/czp3009/bilibili-api
data class LoginResponse(
    @JsonProperty("code")
    var code: Int, // 0
    @JsonProperty("message")
    var message: String?,
    @JsonProperty("data")
    var `data`: Data,
    @JsonProperty("ts")
    var ts: Long // 1550219689
) {
    data class Data(
        @JsonProperty("cookie_info")
        var cookieInfo: CookieInfo,
        @JsonProperty("sso")
        var sso: List<String>,
        @JsonProperty("status")
        var status: Int, // 0
        @JsonProperty("token_info")
        var tokenInfo: TokenInfo,
        @JsonProperty("url")
        var url: String?
    ) {
        data class CookieInfo(
            @JsonProperty("cookies")
            var cookies: List<Cookie>,
            @JsonProperty("domains")
            var domains: List<String>
        ) {
            data class Cookie(
                @JsonProperty("expires")
                var expires: Long, // 1552811689
                @JsonProperty("http_only")
                var httpOnly: Int, // 1
                @JsonProperty("name")
                var name: String, // SESSDATA
                @JsonProperty("value")
                var value: String // 5ff9ba24%2C1552811689%2C04ae9421
            )
        }

        data class TokenInfo(
            @JsonProperty("access_token")
            var accessToken: String, // fd0303ff75a6ec6b452c28f4d8621021
            @JsonProperty("expires_in")
            var expiresIn: Long, // 2592000
            @JsonProperty("mid")
            var mid: Long, // 20293030
            @JsonProperty("refresh_token")
            var refreshToken: String // 6a333ebded3c3dbdde65d136b3190d21
        )
    }

    //快捷方式
    val userId get() = data.tokenInfo.mid
    val token get() = data.tokenInfo.accessToken
}