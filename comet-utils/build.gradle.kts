import java.util.*

plugins {
    `comet-conventions`
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        name = "Compose for Desktop DEV"
    }
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
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly("org.jsoup:jsoup:1.15.3")
    compileOnly("org.jetbrains.skiko:skiko:0.7.40")
    compileOnly("com.aayushatharva.brotli4j:brotli4j:1.8.0")
    api("com.jayway.jsonpath:json-path:2.7.0")
    api("io.pebbletemplates:pebble:3.1.6")
    compileOnly("com.squareup.okio:okio:3.2.0")
    implementation("moe.sdl.yac:core:1.0.1")
}
