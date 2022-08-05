package ren.natsuyuk1.comet.network.thirdparty.bangumi

import io.ktor.http.*
import kotlinx.atomicfu.atomic
import java.util.concurrent.CopyOnWriteArrayList

object Crawler {
    val defaultProtocol by atomic(URLProtocol.HTTPS)

    val userAgents: MutableList<String> = CopyOnWriteArrayList(
        /* ktlint-disable max-line-length */
        arrayListOf(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_3_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.3 Safari/605.1.15",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 12.3; rv:99.0) Gecko/20100101 Firefox/99.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_3_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75",
        )
        /* ktlint-enable no-wildcard-imports */
    )
}
