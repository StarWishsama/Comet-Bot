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
}

dependencies {
    compileOnly(project(":comet-api"))
    compileOnly(project(":comet-core"))
    compileOnly(project(":comet-utils"))

    compileOnly(libs.jline)

    implementation(libs.mirai.core)
    implementation(libs.mirai.core.api)
    implementation(libs.mirai.core.utils)
}

buildConfig {
    packageName("ren.natsuyuk1.comet.mirai")
    useKotlinOutput { topLevelConstants = true }
    string("miraiVersion", libs.mirai.core.api.get().version ?: "Unknown")
}

tasks.shadowJar {
    destinationDirectory.set(File("$rootDir/comet/modules"))
}
