package io.github.starwishsama.nbot.objects.group

import com.google.gson.annotations.SerializedName
import java.util.*

class GroupConfig(@SerializedName("group_id") val groupId: Long) {

    @SerializedName("auto_accept")
    val autoAccept = false

    val admins: List<Long> = LinkedList()

    @SerializedName("mc_server_info")
    val mcServerInfo = false

    @SerializedName("mc_server_address")
    val mcServerAddress: String? = null

}