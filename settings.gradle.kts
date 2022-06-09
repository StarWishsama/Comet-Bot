rootProject.name = "Comet-Bot"
include("comet-core")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.40.2"
}
