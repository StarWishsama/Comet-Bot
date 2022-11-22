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
    maven("https://jitpack.io")
}

val tgbotAPI = "4.1.2"

dependencies {
    compileOnly(project(":comet-api"))
    compileOnly(project(":comet-core"))
    compileOnly(project(":comet-utils"))

    compileOnly("org.jline:jline:3.21.0")

    implementation("dev.inmo:tgbotapi:$tgbotAPI") {
        exclude("io.ktor")
    }
}

buildConfig {
    packageName("ren.natsuyuk1.comet.telegram")
    useKotlinOutput { topLevelConstants = true }
    string("tgbotAPI", tgbotAPI)
}

tasks.shadowJar {
    destinationDirectory.set(File("$rootDir/comet/modules"))
}
