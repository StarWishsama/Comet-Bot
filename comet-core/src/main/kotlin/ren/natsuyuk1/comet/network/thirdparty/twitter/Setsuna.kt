package ren.natsuyuk1.comet.network.thirdparty.twitter

import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.objects.config.TwitterConfig
import ren.natsuyuk1.setsuna.SetsunaClient
import kotlin.coroutines.CoroutineContext

lateinit var client: SetsunaClient

fun initSetsuna(context: CoroutineContext) {
    client = SetsunaClient(cometClient.client, context, TwitterConfig.data.token)
}
