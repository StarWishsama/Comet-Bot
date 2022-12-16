package ren.natsuyuk1.comet.commands.service

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.registerTimeout
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.PictureSearchSource
import ren.natsuyuk1.comet.network.thirdparty.ascii2d.Ascii2DApi
import ren.natsuyuk1.comet.network.thirdparty.saucenao.SauceNaoApi
import ren.natsuyuk1.comet.network.thirdparty.saucenao.data.toMessageWrapper
import ren.natsuyuk1.comet.objects.command.picturesearch.PictureSearchConfigTable
import ren.natsuyuk1.comet.util.toMessageWrapper
import kotlin.time.Duration.Companion.seconds

object PictureSearchService {
    suspend fun handleSearch(subject: PlatformCommandSender, user: CometUser) {
        newSuspendedTransaction {
            if (PictureSearchConfigTable.getPlatform(user.id.value) == null) {
                PictureSearchConfigTable.setPlatform(user.id.value, PictureSearchSource.SAUCENAO)
            }
        }

        PictureSearchQuerySession(
            subject,
            user,
            newSuspendedTransaction { PictureSearchConfigTable.getPlatform(user.id.value) }!!
        ).registerTimeout(30.seconds)

        subject.sendMessage("⏳ 接下来, 请发送你要搜索的图片".toMessageWrapper())
    }

    suspend fun searchImage(platform: LoginPlatform, image: Image, source: PictureSearchSource): MessageWrapper =
        when (source) {
            PictureSearchSource.SAUCENAO -> {
                SauceNaoApi.searchByImage(image).toMessageWrapper(!platform.needRestrict)
            }

            PictureSearchSource.ASCII2D -> {
                Ascii2DApi.searchByImage(image, !platform.needRestrict)
            }
        }
}

class PictureSearchQuerySession(
    contact: PlatformCommandSender,
    cometUser: CometUser?,
    val source: PictureSearchSource
) : Session(contact, cometUser) {
    override suspend fun process(message: MessageWrapper) {
        val image = message.find<Image>()

        if (image == null) {
            contact.sendMessage("请发送图片! 使用 /ps 重新发起搜索请求.".toMessageWrapper())
        } else {
            contact.sendMessage("🔍 请稍等...".toMessageWrapper())
            contact.sendMessage(PictureSearchService.searchImage(contact.platform, image, source))
        }

        expire()
    }
}
