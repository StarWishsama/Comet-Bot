package io.github.starwishsama.comet.service.pusher.instances

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.github.PushEvent
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.stream.Collectors

object GithubPusher {
    fun push(event: PushEvent) {
        val consumer = GroupConfigManager.getAllConfigs().parallelStream().filter {
            it.githubRepoSubscribers.contains(event.repoInfo.fullName)
        }.collect(Collectors.toList())

        val bot = BotVariables.comet.getBot()

        runBlocking {
            consumer.forEach {
                bot.getGroup(it.id)?.also { g ->
                    g.sendMessage(
                        event.toMessageWrapper().toMessageChain(g)
                    )
                }
                delay(RandomUtil.randomLong(10, 400))
            }
        }
    }
}