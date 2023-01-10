/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

@file:Suppress("GradlePackageUpdate")

plugins {
    `comet-conventions`
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.mirai.mamoe.net/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":comet-api"))
    implementation(project(":comet-utils"))

    implementation(libs.kotlinx.datetime)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.client.websockets.jvm)

    implementation(libs.jsoup)

    implementation(libs.yabapi.core.jvm)
    implementation(libs.ipdb.core)

    implementation(libs.setsuna)

    implementation(libs.rome)

    implementation(libs.skiko)
    implementation(libs.brotli4j)
    implementation(libs.okio)

    testCompileOnly(libs.jline)

    implementation(libs.cron.utils)
    implementation(libs.hutool.http)
    implementation(libs.hutool.cron)
    implementation(libs.hutool.crypto)
}
