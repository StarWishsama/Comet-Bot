/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
    implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.0.3")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
}

sourceSets {
    main {
        groovy {
            setSrcDirs(emptySet<File>()) // No Groovy
        }
        java {
            setSrcDirs(setOf("kotlin")) // No Java
        }
    }
    test {
        groovy {
            setSrcDirs(emptySet<File>())
        }
        java {
            setSrcDirs(setOf("kotlin"))
        }
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}
