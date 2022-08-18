/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

plugins {
    kotlin("plugin.serialization") apply false
}

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "ren.natsuyuk1.comet"
    version = "0.7.0-SNAPSHOT"
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "comet-conventions")
}

task("buildComet") {
    println("Now building comet $version...")
    dependsOn(project("comet-console").tasks.findByName("shadowJar"))
    dependsOn(project("comet-mirai-wrapper").tasks.findByName("shadowJar"))
    dependsOn(project("comet-telegram-wrapper").tasks.findByName("shadowJar"))
}
