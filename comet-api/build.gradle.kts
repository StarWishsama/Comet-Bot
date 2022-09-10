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
    implementation(KotlinX.serialization.json)
    implementation(libs.yamlkt)

    api(JetBrains.exposed.core)
    api(JetBrains.exposed.dao)
    api(JetBrains.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)

    implementation(libs.sqlite.jdbc)
    implementation(libs.postgresql)

    implementation(project(":comet-utils"))
}
