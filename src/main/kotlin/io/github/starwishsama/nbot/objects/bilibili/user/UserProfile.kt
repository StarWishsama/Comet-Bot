package io.github.starwishsama.nbot.objects.bilibili.user

import com.google.gson.annotations.SerializedName




data class UserProfile(val info: Info) {
    open class Info {
        var uid: Int = 0
        @SerializedName("uname")
        open var userName: String = ""
        @SerializedName("face")
        open var avatarImgURL: String = ""

        constructor()

        constructor(uid: Int, userName: String, avatarImgURL: String){
            this.uid = uid
            this.userName = userName
            this.avatarImgURL = avatarImgURL
        }
    }
}