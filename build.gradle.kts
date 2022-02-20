/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    id("net.mamoe.mirai-console") version Versions.miraiVersion
}

group = "io.github.starwishsama.comet"
version = "0.6.5" + getGitInfo()

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.starwishsama.comet.CometApplication"
        attributes["Author"] = "StarWishsama"
    }
}

repositories {
    mavenCentral()
    google()
    maven(url = "https://maven.aliyun.com/repository/public/")
    gradlePluginPortal()
    maven(url = "https://maven.aliyun.com/nexus/content/repositories/central/")
    maven(url = "https://jitpack.io")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("cn.hutool:hutool-http:${Versions.hutoolVersion}")
    implementation("cn.hutool:hutool-crypto:${Versions.hutoolVersion}")

    // yamlkt @ https://github.com/him188/yamlkt
    implementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlktVersion}")

    // jackson @ https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation("com.fasterxml.jackson.core:jackson-core:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jacksonVersion}")

    // CUrl
    implementation("com.github.rockswang:java-curl:1.2.2.190107")

    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.14.3")

    // Retrofit A type-safe HTTP client for Android and Java @ https://github.com/square/retrofit/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")

    // DNSJava used to srv lookup
    implementation("dnsjava:dnsjava:3.5.0")

    implementation("io.ktor:ktor-server-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${Versions.ktorVersion}")

    implementation("com.github.StarWishsama:rkon-core:master-SNAPSHOT")

    implementation("moe.sdl.yabapi:yabapi-core-jvm:${Versions.yabapiVersion}") {
        exclude("org.jetbrains.kotlinx", "atomicfu-jvm")
        exclude("org.jetbrains.kotlinx", "atomicfu")
    }

    // Fix yabapi
    implementation("org.jetbrains.kotlinx:atomicfu:0.17.0")

    testImplementation(kotlin("test"))
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

buildConfig {
    println("Comet >> Generating comet information.....")

    packageName("io.github.starwishsama.comet")
    buildConfigField("String", "version", "\"${project.version}\"")
    buildConfigField(
        "String",
        "buildTime",
        "\"${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))}\""
    )
    buildConfigField("String", "miraiVersion", "\"${Versions.miraiVersion}\"")
}

fun getGitInfo(): String {
    val commitHashCommand = "git rev-parse --short HEAD"
    val commitHash = Runtime.getRuntime().exec(commitHashCommand).inputStream.bufferedReader().readLine()

    val branchCommand = "git rev-parse --abbrev-ref HEAD"
    var branch = Runtime.getRuntime().exec(branchCommand).inputStream.bufferedReader().readLine()

    if (branch.isEmpty()) {
        branch = "UnknownBranch"
    }

    return "-$branch-$commitHash"
}

tasks.test {
    useJUnitPlatform()
}

mirai {
    configureShadow {
        dependsOn(tasks.generateBuildConfig)
        isZip64 = true
        exclude("META-INF/*.txt")
        exclude("META-INF/*.md")
        exclude("META-INF/CHANGES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")

        println("Comet >> Using Java ${System.getProperty("java.version")} to build.")
        println("Comet >> Now building Comet ${project.version}...")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}
