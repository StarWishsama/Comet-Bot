import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

fun BuildConfigSourceSet.string(name: String, value: String) = buildConfigField("String", name, "\"$value\"")
fun BuildConfigSourceSet.stringNullable(name: String, value: String?) =
    buildConfigField("String?", name, value?.let { "\"$value\"" } ?: "null")

fun BuildConfigSourceSet.long(name: String, value: Long) = buildConfigField("long", name, value.toString())
fun BuildConfigSourceSet.longNullable(name: String, value: Long?) =
    buildConfigField("Long?", name, value?.let { "$value" } ?: "null")

fun BuildConfigSourceSet.int(name: String, value: Int) = buildConfigField("int", name, value.toString())
fun BuildConfigSourceSet.intNullable(name: String, value: Int?) =
    buildConfigField("int", name, value?.let { "$value" } ?: "null")

fun BuildConfigSourceSet.boolean(name: String, value: Boolean) = buildConfigField("boolean", name, value.toString())
