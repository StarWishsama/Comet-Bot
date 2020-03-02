package io.github.starwishsama.nbot.objects

class ShopItem {
    var itemName: String
    var needPoint: Int
    var itemCommand: String? = null
    var buyTime: Int
    var money = 0

    constructor(name: String, point: Int, times: Int, itemCommand: String?) {
        itemName = name
        needPoint = point
        buyTime = times
        this.itemCommand = itemCommand
    }

    constructor(name: String, point: Int, times: Int, money: Int) {
        itemName = name
        needPoint = point
        buyTime = times
        this.money = money
    }
}