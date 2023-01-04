package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.http.*
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.isBlank
import ren.natsuyuk1.comet.utils.file.isType
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File

object ProjectSekaiEvent {
    private val eventImage = pjskFolder.resolve("event/")

    init {
        eventImage.mkdir()
    }

    fun getEventTeamImage(num: Int): File? {
        val target = eventImage.resolve("team_$num.png")

        return if (target.isBlank() || !target.isType("image/png")) {
            null
        } else {
            target
        }
    }

    suspend fun updateEventTeamImage(eventName: String) {
        cometClient.client.downloadFile(
            buildAssetUrl(eventName, 1),
            eventImage.resolve("team_1.png").also { it.touch() }
        ) {
            it.contentType()?.match(ContentType.Image.PNG) == true
        }

        cometClient.client.downloadFile(
            buildAssetUrl(eventName, 2),
            eventImage.resolve("team_2.png").also { it.touch() }
        ) {
            it.contentType()?.match(ContentType.Image.PNG) == true
        }
    }

    private fun buildAssetUrl(eventName: String, num: Int): String =
        "https://storage.sekai.best/sekai-assets/event/$eventName/team_image_rip/${eventName}_item_$num.png"
}
