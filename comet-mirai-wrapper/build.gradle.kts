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
}

dependencies {
    implementation(project(":comet-api"))
    implementation(project(":comet-core"))
    implementation(project(":comet-utils"))

    implementation(KotlinX.serialization.json)
    implementation("net.mamoe.yamlkt:yamlkt:_")

    implementation("net.mamoe:mirai-core-all:_") {
        exclude("org.jetbrains.kotlinx", "atomicfu-jvm")
    }

    api("org.jetbrains.exposed:exposed-core:_")
    api("org.jetbrains.exposed:exposed-dao:_")
    api("org.jetbrains.exposed:exposed-jdbc:_")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    implementation("com.zaxxer:HikariCP:_")
}