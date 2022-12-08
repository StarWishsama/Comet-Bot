package ren.natsuyuk1.comet.utils.system

fun getEnv(propName: String): String? {
    val env = propName.replace(".".toRegex(), "_").replace("-".toRegex(), "_")
    return getEnv(propName, env)
}

fun getEnv(propName: String, env: String): String? = System.getProperty(propName) ?: System.getenv(env)
