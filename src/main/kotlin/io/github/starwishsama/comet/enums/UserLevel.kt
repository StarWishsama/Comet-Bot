package io.github.starwishsama.comet.enums

import io.github.starwishsama.comet.objects.BotUser

enum class UserLevel {
    USER, VIP, ADMIN, OWNER, CONSOLE;

    companion object {
        fun upgrade(user: BotUser): UserLevel {
            user.level = values()[user.level.ordinal + 1]
            return user.level
        }
    }
}