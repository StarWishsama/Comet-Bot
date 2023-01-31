import java.util.*

plugins {
    `comet-conventions`
    kotlin("plugin.serialization")
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.mirai.mamoe.net/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

fun tokenize(command: String): Array<String?> {
    val st = StringTokenizer(command)
    val cmdArray = arrayOfNulls<String>(st.countTokens())

    var i = 0
    while (st.hasMoreTokens()) {
        cmdArray[i] = st.nextToken()
        i++
    }

    return cmdArray
}

val commitHash by lazy {
    val commitHashCommand = "git rev-parse --short HEAD"
    Runtime.getRuntime().exec(tokenize(commitHashCommand)).inputStream.bufferedReader().readLine() ?: "Unknown Commit"
}

val branch by lazy {
    val branchCommand = "git rev-parse --abbrev-ref HEAD"
    Runtime.getRuntime().exec(tokenize(branchCommand)).inputStream.bufferedReader().readLine() ?: "Unknown Branch"
}

buildConfig {
    println("Comet >> Generating comet information.....")

    packageName("ren.natsuyuk1.comet.config")
    useKotlinOutput { topLevelConstants = true }
    string("version", version.toString())
    string("branch", branch)
    string("hash", commitHash)
}

dependencies {
    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.dao)
    compileOnly(libs.exposed.jdbc)
    compileOnly(libs.exposed.kotlin.datetime)
    testImplementation(libs.exposed.kotlin.datetime)
    compileOnly(libs.hikaricp)
    compileOnly(libs.jsoup)
    compileOnly(libs.skiko)
    compileOnly(libs.brotli4j)
    api(libs.jsonpath)
    api(libs.pebble)
    compileOnly(libs.okio)
    compileOnly(libs.yac.core)

    compileOnly(libs.hutool.http)
    compileOnly(libs.hutool.cron)
    compileOnly(libs.hutool.crypto)

    compileOnly(libs.ktor.server.core)
    compileOnly(libs.ktor.server.netty)
    compileOnly(libs.ktor.server.rate.limit)
    compileOnly(libs.ktor.server.call.logging.jvm)

    compileOnly(libs.ktor.client.websockets.jvm)
    compileOnly(libs.ktor.client.cio)
    compileOnly(libs.ktor.client.core)
    compileOnly(libs.ktor.client.logging)
    compileOnly(libs.ktor.client.encoding)
    compileOnly(libs.ktor.client.content.negotiation)
    compileOnly(libs.ktor.serialization.kotlinx.json)
    compileOnly(libs.ktor.network)

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    api("net.mamoe.yamlkt:yamlkt:0.12.0")
}
