/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
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

dependencies {
    implementation("moe.sdl.yac:core:1.0.0")
    implementation("org.jline:jline:3.21.0")

    api(project(":comet-api"))
    api(project(":comet-core"))
    api(project(":comet-utils"))

    compileOnly("org.jetbrains.skiko:skiko:0.7.27")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ren.natsuyuk1.comet.console.CometTerminalKt"
        attributes["Author"] = "StarWishsama"
    }
}

tasks.shadowJar {
    archiveFileName.set("${project.name}.jar")
    destinationDirectory.set(File("$rootDir/comet"))
}
