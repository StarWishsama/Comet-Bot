package ren.natsuyuk1.comet.utils.system

fun getEnv(propName: String): String? {
    val env = propName.replace("\\.".toRegex(), "_").replace("\\-".toRegex(), "_").uppercase()
    return getEnv(propName, env)
}

fun getEnv(propName: String, env: String): String? =
    System.getProperty(propName).also { println("prop($propName): $it") } ?: System.getenv(env)
        .also { println("env($env): $it") }
