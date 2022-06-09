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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
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

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "17"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
