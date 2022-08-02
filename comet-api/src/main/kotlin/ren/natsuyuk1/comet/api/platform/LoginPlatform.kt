package ren.natsuyuk1.comet.api.platform

import kotlinx.serialization.Serializable

@Serializable
enum class LoginPlatform {
    /**
     * Represent to `comet-mirai-wrapper`
     */
    MIRAI,

    /**
     * Represent to `comet-telegram-wrapper`
     */
    TELEGRAM,

    /**
     * Represent to unit test
     */
    TEST
}
