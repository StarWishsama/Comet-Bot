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

val ktor = "2.2.1"

dependencies {
    api(project(":comet-api"))
    api(project(":comet-utils"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("io.ktor:ktor-server-core:$ktor")
    implementation("io.ktor:ktor-server-netty:$ktor")
    implementation("io.ktor:ktor-server-rate-limit:$ktor")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor")
    implementation("io.ktor:ktor-client-websockets-jvm:$ktor")

    implementation("org.jsoup:jsoup:1.15.3")

    implementation("moe.sdl.yabapi:yabapi-core-jvm:0.11.1")

    implementation("moe.sdl.ipdb:ipdb-core:0.2.1")

    implementation("ren.natsuyuk1.setsuna:Setsuna:0.1.0-SNAPSHOT")

    implementation("com.rometools:rome:1.18.0")

    implementation("org.jetbrains.skiko:skiko:0.7.40")
    implementation("com.aayushatharva.brotli4j:brotli4j:1.9.0")
    implementation("com.squareup.okio:okio:3.2.0")

    testCompileOnly("org.jline:jline:3.21.0")
}
