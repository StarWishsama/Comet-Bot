package io.github.starwishsama.comet.objects.group

import com.google.gson.annotations.SerializedName

class PerGroupConfig(@SerializedName("group_id") val groupId: Long) {

    @SerializedName("auto_accept")
    var autoAccept: Boolean = false

    @SerializedName("group_helpers")
    var helpers: Set<Long> = HashSet()

    @SerializedName("twitter_push_function")
    var twitterPushEnabled: Boolean = false

    @SerializedName("twitter_sub")
    var twitterSubscribers: Set<String> = HashSet()

    @SerializedName("bili_push_function")
    var biliPushEnabled: Boolean = false

    @SerializedName("bili_sub")
    var biliSubscribers: Set<Int> = HashSet()

    @SerializedName("youtube_push_function")
    var youtubePushEnabled: Boolean = false

    @SerializedName("youtube_sub")
    var youtubeSubscribers: Set<String> = HashSet()

    /**
     * 是否关闭对此群消息的复读
     */
    @SerializedName("repeat_function")
    var doRepeat: Boolean = true

    fun addHelper(id: Long): Boolean {
        if (isHelper(id)) return false
        helpers = helpers.plusElement(id)
        return true
    }

    fun removeHelper(id: Long): Boolean {
        if (!isHelper(id)) return false
        helpers = helpers.minusElement(id)
        return true
    }

    fun isHelper(id: Long): Boolean {
        return helpers.contains(id)
    }
}