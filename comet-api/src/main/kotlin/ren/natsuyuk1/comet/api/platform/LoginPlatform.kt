package ren.natsuyuk1.comet.api.platform

import kotlinx.serialization.Serializable

@Serializable
enum class LoginPlatform(val needRestrict: Boolean = false) {
    /**
     * Represent to `comet-mirai-wrapper`
     */
    MIRAI(true),

    /**
     * Represent to `comet-telegram-wrapper`
     */
    TELEGRAM,

    /**
     * Represent to unit test
     */
    TEST
}
