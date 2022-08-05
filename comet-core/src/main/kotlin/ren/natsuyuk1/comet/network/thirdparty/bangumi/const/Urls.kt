@file:Suppress("NOTHING_TO_INLINE")

package ren.natsuyuk1.comet.network.thirdparty.bangumi.const

import ren.natsuyuk1.comet.network.thirdparty.bangumi.Crawler

internal const val BANGUMI_DOMAIN = "bangumi.tv"

internal inline val MAIN_DOMAIN
    get() = "${Crawler.defaultProtocol.name}://$BANGUMI_DOMAIN"

internal inline val BANGUMI_SUBJECT
    get() = "$MAIN_DOMAIN/subject/"

internal inline fun buildSubjectUrl(id: Long) = BANGUMI_SUBJECT + id
