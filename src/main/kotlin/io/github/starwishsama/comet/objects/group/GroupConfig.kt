package io.github.starwishsama.comet.objects.group

import com.google.gson.annotations.SerializedName
import java.util.*

class GroupConfig(@SerializedName("group_id") val groupId: Long) {

    @SerializedName("auto_accept")
    val autoAccept = false

    val admins: List<Long> = LinkedList()

}