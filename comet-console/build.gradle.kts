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
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jline:jline:3.21.0")

    implementation(project(":comet-api"))
    implementation(project(":comet-core"))
    implementation(project(":comet-utils"))

    implementation("org.jetbrains.skiko:skiko:0.7.40")
    testImplementation("org.jetbrains.skiko:skiko:0.7.40")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ren.natsuyuk1.comet.console.CometTerminalKt"
        attributes["Author"] = "StarWishsama"
    }
}

tasks.shadowJar {
    archiveFileName.set("${project.name}.jar")
    destinationDirectory.set(File("$rootDir/comet"))

    exclude("checkstyle.xml")
    exclude("**/*.html")
    exclude("CronUtilsI18N*.properties")
    exclude("DebugProbesKt.bin")
    exclude("org/sqlite/native/FreeBSD/**/*")
    exclude("org/sqlite/native/Linux-Android/**/*")
    exclude("org/sqlite/native/Linux-Musl/**/*")
    listOf("arm", "armv6", "armv7", "ppc64", "x86").forEach {
        exclude("org/sqlite/native/Linux/$it/**/*")
        exclude("org/sqlite/native/Windows/$it/**/*")
    }
    listOf("freebsd32", "freebsd64", "linux32", "windows32").forEach {
        exclude("META-INF/native/$it/**/*")
    }
    listOf("aix", "freebsd", "openbsd", "sunos").forEach {
        exclude("com/sun/jna/$it*/**/*")
    }
    listOf("arm", "armel", "loongarch64", "mips64el", "ppc", "ppc64le", "riscv64", "s390x", "x86").forEach {
        exclude("com/sun/jna/linux-$it/**/*")
        exclude("com/sun/jna/win32-$it/**/*")
    }
}
