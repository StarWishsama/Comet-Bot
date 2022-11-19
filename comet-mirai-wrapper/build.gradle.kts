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
    // maven("https://repo.mirai.mamoe.net/snapshots")
}

dependencies {
    compileOnly(project(":comet-api"))
    compileOnly(project(":comet-core"))
    compileOnly(project(":comet-utils"))

    compileOnly("org.jline:jline:3.21.0")

    // use fixed version for resolving deps relocate issue
    // see https://github.com/mamoe/mirai/pull/2331
    implementation("net.mamoe:mirai-core:2.14.0-dev-shadow-1")
    implementation("net.mamoe:mirai-core-api:2.14.0-dev-shadow-1")
    implementation("net.mamoe:mirai-core-utils:2.14.0-dev-shadow-1")
}

tasks.shadowJar {
    destinationDirectory.set(File("$rootDir/comet/modules"))
}
