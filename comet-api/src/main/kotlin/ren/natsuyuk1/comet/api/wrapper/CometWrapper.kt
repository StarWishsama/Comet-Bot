package ren.natsuyuk1.comet.api.wrapper

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig

interface CometWrapper {
    suspend fun createInstance(config: CometConfig): Comet
}
