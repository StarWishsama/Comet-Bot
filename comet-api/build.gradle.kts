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
}

dependencies {
    implementation(KotlinX.serialization.json)

    implementation("cn.hutool:hutool-http:_")
    implementation("cn.hutool:hutool-crypto:_")
    implementation("cn.hutool:hutool-cron:_")

    api("org.jetbrains.exposed:exposed-core:_")
    api("org.jetbrains.exposed:exposed-dao:_")
    api("org.jetbrains.exposed:exposed-jdbc:_")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    implementation("com.zaxxer:HikariCP:_")

    implementation(project(":comet-utils"))
}
