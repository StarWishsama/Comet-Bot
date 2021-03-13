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