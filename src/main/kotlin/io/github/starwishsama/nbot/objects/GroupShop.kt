package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.BotConstants
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


class GroupShop {
    var groupId: Long = 0
    val admins: MutableList<BotUser>? = null
    var items: ArrayList<ShopItem?>? = ArrayList()

    constructor() {}

    constructor(groupId: Long) {
        this.groupId = groupId
    }

    fun getItemByName(itemName: String): ShopItem? {
        val result = AtomicReference<ShopItem?>()
        items!!.forEach(Consumer { item: ShopItem? ->
            if (item!!.itemName == itemName) {
                result.set(item)
            }
        })
        return result.get()
    }

    fun addAdmin(user: BotUser) {
        admins!!.add(user)
    }

    fun removeAdmin(user: BotUser) {
        admins!!.remove(user)
    }

    fun addNewItem(item: ShopItem) {
        if (items == null) {
            items = ArrayList()
        }
        if (!items!!.contains(item)) {
            items!!.add(item)
        }
    }

    companion object {
        fun getShopById(groupId: Long): GroupShop {
            val result = AtomicReference(GroupShop())
            BotConstants.shop.forEach { shop ->
                if (shop.groupId == groupId) {
                    result.set(shop)
                }
            }
            return result.get()
        }
    }
}
