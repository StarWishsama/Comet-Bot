package io.github.starwishsama.comet.objects.group

import io.github.starwishsama.comet.BotConstants
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.ShopItem
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


class Shop {
    var groupId: Long = 0
    val admins: MutableList<BotUser> = mutableListOf()
    var items: ArrayList<ShopItem> = ArrayList()

    constructor()

    constructor(groupId: Long) {
        this.groupId = groupId
    }

    fun getItemByName(itemName: String): ShopItem {
        val result = AtomicReference<ShopItem>()
        items.forEach(Consumer { item ->
            if (item.itemName == itemName) {
                result.set(item)
            }
        })
        return result.get()
    }

    fun addAdmin(user: BotUser) {
        admins.add(user)
    }

    fun removeAdmin(user: BotUser) {
        admins.remove(user)
    }

    fun addNewItem(item: ShopItem) {
        if (!items.contains(item)) {
            items.add(item)
        }
    }

    companion object {
        fun getShopById(groupId: Long): Shop {
            val result = AtomicReference(Shop())
            BotConstants.shop.forEach { shop ->
                if (shop.groupId == groupId) {
                    result.set(shop)
                }
            }
            return result.get()
        }
    }
}
