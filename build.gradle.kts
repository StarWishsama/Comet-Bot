/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ktlint.idea)
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

installGitHooks()

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "comet-conventions")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
        filter {
            // exclude("**/generated/**")
            fun exclude(path: String) = exclude {
                projectDir.toURI().relativize(it.file.toURI()).normalize().path.contains(path)
            }
            setOf("/generated/", "/build/", "resources").forEach { exclude(it) }
        }
    }
}

task("buildComet") {
    group = "build"
    val output = File("$rootDir/comet")

    if (output.isDirectory && !output.listFiles().isNullOrEmpty()) {
        output.deleteRecursively()
    }

    println("Now building comet $version...")
    dependsOn(project("comet-console").tasks.findByName("shadowJar"))
    dependsOn(project("comet-mirai-wrapper").tasks.findByName("shadowJar"))
    dependsOn(project("comet-telegram-wrapper").tasks.findByName("shadowJar"))
}
