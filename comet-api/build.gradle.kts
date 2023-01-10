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

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.mirai.mamoe.net/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly(libs.jline)
    testCompileOnly(libs.jline)

    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)
    implementation(libs.sqlite.jdbc)
    implementation(libs.postgresql)
    api(libs.yac.core)
    implementation(libs.cron.utils)
    implementation(libs.hutool.http)
    implementation(libs.hutool.cron)
    implementation(libs.hutool.crypto)

    compileOnly(libs.ktor.client.core)

    implementation(project(":comet-utils"))
}

tasks.withType(AbstractTestTask::class.java).configureEach {
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
