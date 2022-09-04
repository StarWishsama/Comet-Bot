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

    api("org.jetbrains.exposed:exposed-core:_")
    api("org.jetbrains.exposed:exposed-dao:_")
    api("org.jetbrains.exposed:exposed-jdbc:_")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    implementation("com.zaxxer:HikariCP:_")

    implementation("io.ktor:ktor-server-core:_")
    implementation("io.ktor:ktor-server-netty:_")
    implementation("io.ktor:ktor-server-call-logging:_")
    implementation("io.ktor:ktor-client-websockets:_")

    implementation("org.jsoup:jsoup:_")

    implementation("moe.sdl.yabapi:yabapi-core-jvm:_")

    implementation("org.jetbrains.skiko:skiko:_")

    implementation("com.aayushatharva.brotli4j:brotli4j:1.7.1")

    implementation("ren.natsuyuk1.setsuna:Setsuna:_")

    implementation("com.rometools:rome:_")
}
