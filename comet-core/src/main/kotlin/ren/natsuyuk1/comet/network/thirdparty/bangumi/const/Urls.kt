@file:Suppress("NOTHING_TO_INLINE")

package ren.natsuyuk1.comet.network.thirdparty.bangumi.const

import io.ktor.http.*

internal const val BANGUMI_DOMAIN = "bangumi.tv"

internal inline val MAIN_DOMAIN
    get() = "${URLProtocol.HTTPS.name}://$BANGUMI_DOMAIN"

internal inline val BANGUMI_SUBJECT
    get() = "$MAIN_DOMAIN/subject/"

internal inline fun buildSubjectUrl(id: Long) = BANGUMI_SUBJECT + id
