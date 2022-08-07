/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("com.github.johnrengelman.shadow")
    java
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.github.microutils:kotlin-logging-jvm:_")
    implementation("ch.qos.logback:logback-classic:_")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

    testImplementation(kotlin("test"))

    implementation("moe.sdl.yac:core:_")

    implementation("cn.hutool:hutool-http:_")
    implementation("cn.hutool:hutool-crypto:_")
    implementation("cn.hutool:hutool-cron:_")

    // Ktor FIXME: USING 1.X now, migrate to v2 when mirai supports
    implementation("io.ktor:ktor-client-core:_")
    implementation("io.ktor:ktor-client-cio:_")
    implementation("io.ktor:ktor-client-logging:_")
    implementation("io.ktor:ktor-client-encoding:_")
    implementation("io.ktor:ktor-client-serialization:_")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
    implementation("net.mamoe.yamlkt:yamlkt:_")
}

sourceSets {
    main {
        java {
            setSrcDirs(setOf("kotlin")) // No Java, and Kotlin Only
        }
    }
    test {
        java {
            setSrcDirs(setOf("kotlin")) // No Java, and Kotlin Only
        }
    }
}

tasks.test {
    dependsOn("generateTestBuildConfig")
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "17"

        OptInAnnotations.list.forEach {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=$it"
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    create("test")
}

tasks.register<Jar>("testArchive") {
    archiveBaseName.set("${project.name}-test")
    from(project.the<SourceSetContainer>()["test"].output)
}

artifacts {
    add("test", tasks["testArchive"])
}
