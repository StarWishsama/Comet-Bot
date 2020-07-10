package io.github.starwishsama.comet.objects.pojo.twitter

import com.google.gson.annotations.SerializedName

data class TwitterErrorInfo(val errors: List<TwitterErrorReason>) {
    data class TwitterErrorReason(val code: Int, @SerializedName("message") val reason : String)
    fun getReason() : String {
        val reason = StringBuilder()
        errors.forEach {
            reason.append("代码: ${it.code}, 信息: ${it.reason}\n")
        }
        return reason.toString().trim()
    }
}