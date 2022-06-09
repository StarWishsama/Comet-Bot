import gradle.kotlin.dsl.accessors._9ee6941c65ba2f24339c75cc9cde03b1.ext
import org.gradle.api.Project
import java.util.*

fun Project.getRootProjectLocalProps(): Map<String, String> {
    val file = project.rootProject.file("local.properties")
    return if (file.exists()) {
        file.reader().use {
            Properties().apply {
                load(it)
            }
        }.toMap().map {
            it.key.toString() to it.value.toString()
        }.toMap()
    } else emptyMap()
}

fun Project.getExtraString(name: String) = kotlin.runCatching { ext[name]?.toString() }.getOrNull()

fun Project.getExtraBoolean(name: String) = kotlin.runCatching { ext[name] as Boolean }.getOrNull()
