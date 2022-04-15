@file:Suppress("NOTHING_TO_INLINE")

package io.github.starwishsama.comet.api.thirdparty.bgm.const

import io.github.starwishsama.comet.api.thirdparty.bgm.Crawler

internal const val BANGUMI_DOMAIN = "bangumi.tv"

internal inline val MAIN_DOMAIN
  get() = "${Crawler.defaultProtocol.name}://$BANGUMI_DOMAIN"

internal inline val BANGUMI_SUBJECT
  get() = "$MAIN_DOMAIN/subject/"

internal inline fun buildSubjectUrl(id: Long) = BANGUMI_SUBJECT + id
