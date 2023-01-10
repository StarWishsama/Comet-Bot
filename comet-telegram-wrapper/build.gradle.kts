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

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.mirai.mamoe.net/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly(project(":comet-api"))
    compileOnly(project(":comet-core"))
    compileOnly(project(":comet-utils"))

    compileOnly(libs.jline)

    implementation(libs.tgbotapi)
}

buildConfig {
    packageName("ren.natsuyuk1.comet.telegram")
    useKotlinOutput { topLevelConstants = true }
    string("tgbotAPI", libs.tgbotapi.get().version ?: "Unknown")
}

tasks.shadowJar {
    destinationDirectory.set(File("$rootDir/comet/modules"))
}
