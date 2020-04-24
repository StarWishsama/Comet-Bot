package io.github.starwishsama.nbot.enums

import io.github.starwishsama.nbot.objects.BotUser

enum class UserLevel {
    USER, VIP, ADMIN, OWNER;

    companion object {
        fun upgrade(user: BotUser) : UserLevel {
            user.level = values()[user.level.ordinal + 1]
            return user.level
        }
    }
}