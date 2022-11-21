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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation(("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"))

    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.40.1")
    implementation("com.zaxxer:HikariCP:5.0.1")

    api("io.ktor:ktor-server-core:2.1.3")
    api("io.ktor:ktor-server-netty:2.1.3")
    api("io.ktor:ktor-server-call-logging-jvm:2.1.2")
    api("io.ktor:ktor-client-websockets-jvm:2.1.3")

    implementation("org.jsoup:jsoup:1.15.3")

    implementation("moe.sdl.yabapi:yabapi-core-jvm:0.11.1")

    implementation("moe.sdl.ipdb:ipdb-core:0.1.1")

    implementation("org.jetbrains.skiko:skiko:0.7.27")

    implementation("com.aayushatharva.brotli4j:brotli4j:1.8.0")

    implementation("ren.natsuyuk1.setsuna:Setsuna:0.1.0-SNAPSHOT")

    implementation("com.rometools:rome:1.18.0")
}
