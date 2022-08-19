import java.util.*

plugins {
    `comet-conventions`
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
    compileOnly(KotlinX.serialization.json)
    compileOnly("org.jetbrains.exposed:exposed-core:_")
    compileOnly("org.jetbrains.exposed:exposed-dao:_")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:_")
    compileOnly("org.jetbrains.exposed:exposed-kotlin-datetime:_")
    compileOnly("com.zaxxer:HikariCP:_")
    compileOnly("org.jsoup:jsoup:_")
    compileOnly("org.jetbrains.skiko:skiko:_")
    compileOnly("com.aayushatharva.brotli4j:brotli4j:1.7.1")
}
