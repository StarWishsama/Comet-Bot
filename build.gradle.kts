/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

import com.diffplug.gradle.spotless.FormatExtension

plugins {
    kotlin("jvm") apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "ren.natsuyuk1.comet"
    version = "0.7.1"
}

installGitHooks()

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "comet-conventions")
    apply(plugin = "com.diffplug.spotless")
}

spotless {
    fun FormatExtension.excludes() {
        targetExclude("**/build/", "**/generated/", "**/resources/", "./buildSrc")
    }

    fun FormatExtension.common() {
        trimTrailingWhitespace()
        lineEndings = com.diffplug.spotless.LineEnding.WINDOWS
        endWithNewline()
    }

    val ktlintConfig = mapOf(
        "ij_kotlin_allow_trailing_comma" to "true",
        "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
        "trailing-comma-on-declaration-site" to "true",
        "trailing-comma-on-call-site" to "true",
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_disabled_import-ordering" to "disabled",
        "ktlint_standard_filename" to "disabled",
    )

    kotlin {
        target("**/*.kt")
        excludes()
        common()
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintConfig)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        excludes()
        common()
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintConfig)
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

// agree build scan tos ON ci
if (hasProperty("buildScan") && System.getenv().containsKey("CI")) {
    extensions.findByName("buildScan")?.withGroovyBuilder {
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}
