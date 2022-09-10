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
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

dependencies {
    implementation(project(":comet-api"))
    implementation(project(":comet-utils"))

    implementation(KotlinX.serialization.json)
    implementation(KotlinX.datetime)

    api(JetBrains.exposed.core)
    api(JetBrains.exposed.dao)
    api(JetBrains.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)

    implementation(Ktor.server.core)
    implementation(Ktor.server.netty)
    implementation(Ktor.server.callLogging)
    implementation(libs.ktor.client.websockets)

    implementation(libs.jsoup)

    implementation(libs.yabapi.core.jvm)

    implementation(libs.skiko)

    implementation(libs.brotli4j)

    implementation(libs.setsuna)

    implementation(libs.rome)
}
