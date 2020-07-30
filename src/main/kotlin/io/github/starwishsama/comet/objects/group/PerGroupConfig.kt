package io.github.starwishsama.comet.objects.group

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import java.util.*

data class PerGroupConfig(@SerializedName("group_id") val id: Long) {

    @SerializedName("auto_accept")
    var autoAccept: Boolean = false

    @SerializedName("group_helpers")
    var helpers: HashSet<Long> = HashSet()

    @SerializedName("twitter_push_function")
    var twitterPushEnabled: Boolean = false

    @SerializedName("twitter_sub")
    var twitterSubscribers: HashSet<String> = HashSet()

    @SerializedName("bili_push_function")
    var biliPushEnabled: Boolean = false

    @SerializedName("bili_sub")
    val biliSubscribers: HashSet<Long> = HashSet()

    @SerializedName("youtube_push_function")
    var youtubePushEnabled: Boolean = false

    @SerializedName("youtube_sub")
    val youtubeSubscribers: HashSet<String> = HashSet()

    /**
     * 是否关闭对此群消息的复读
     */
    @SerializedName("repeat_function")
    var doRepeat: Boolean = true

    fun addHelper(id: Long): Boolean {
        if (isHelper(id)) return false
        helpers.add(id)
        return true
    }

    fun removeHelper(id: Long): Boolean {
        if (!isHelper(id)) return false
        helpers.remove(id)
        return true
    }

    fun isHelper(id: Long): Boolean {
        return helpers.contains(id)
    }

    fun init(): PerGroupConfig {
        BotVariables.perGroup.add(this)
        return this
    }
}