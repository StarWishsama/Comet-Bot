package io.github.starwishsama.nbot.objects.pojo.bilibili.user

import com.google.gson.annotations.SerializedName

data class UserProfile(val info: Info) {
    open class Info {
        var uid: Int = 0
        @SerializedName("uname")
        open var userName: String = ""
        @SerializedName("face")
        open var avatarImgURL: String = ""
    }
}