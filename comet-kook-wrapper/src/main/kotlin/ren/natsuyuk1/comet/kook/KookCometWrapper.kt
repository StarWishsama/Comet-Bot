package ren.natsuyuk1.comet.kook

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper

class KookCometWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig): Comet {
        TODO("Not yet implemented")
    }

    override fun platform(): LoginPlatform = LoginPlatform.KOOK

    override fun libInfo(): String = "comet-kook 1.0.0"
}
