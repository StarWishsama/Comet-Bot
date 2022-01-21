/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.login

import com.fasterxml.jackson.annotation.JsonProperty

data class GetKeyResponse(
    @JsonProperty("code")
    var code: Int, // 0
    @JsonProperty("message")
    var message: String?,
    @JsonProperty("data")
    var `data`: Data,
    @JsonProperty("ts")
    var ts: Long // 1550219688
) {
    data class Data(
        @JsonProperty("hash")
        var hash: String, // 93ac6f60b4789952
        @JsonProperty("key")
        var key: String // -----BEGIN PUBLIC KEY-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdScM09sZJqFPX7bvmB2y6i08JbHsa0v4THafPbJN9NoaZ9Djz1LmeLkVlmWx1DwgHVW+K7LVWT5FV3johacVRuV9837+RNntEK6SE82MPcl7fA++dmW2cLlAjsIIkrX+aIvvSGCuUfcWpWFy3YVDqhuHrNDjdNcaefJIQHMW+sQIDAQAB-----END PUBLIC KEY-----
    )
}