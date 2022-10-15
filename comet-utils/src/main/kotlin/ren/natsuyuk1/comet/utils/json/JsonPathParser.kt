package ren.natsuyuk1.comet.utils.json

import com.jayway.jsonpath.DocumentContext
import com.mitchellbosecke.pebble.PebbleEngine
import mu.KotlinLogging
import java.io.StringWriter

private val TEMPLATE_ENGINE: PebbleEngine = PebbleEngine.Builder().build()
private val logger = KotlinLogging.logger {}

class JsonPathMap(private val compiledPath: DocumentContext) : MutableMap<String?, Any?> {
    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsKey(key: String?): Boolean {
        return compiledPath.read(key as String, Any::class.java) == null
    }

    override fun containsValue(value: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override operator fun get(key: String?): Any? {
        return compiledPath.read(key as String)
    }

    override fun put(key: String?, value: Any?): Any? {
        throw UnsupportedOperationException()
    }

    override fun remove(key: String?): Any? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out String?, Any?>) {
        throw UnsupportedOperationException()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String?, Any?>>
        get() = throw UnsupportedOperationException()
    override val keys: MutableSet<String?>
        get() = throw UnsupportedOperationException()
    override val size: Int
        get() = throw UnsupportedOperationException()
    override val values: MutableCollection<Any?>
        get() = throw UnsupportedOperationException()

    override fun clear() {
        throw UnsupportedOperationException()
    }
}

fun JsonPathMap.parsePath(template: String): String {
    val writer = StringWriter()
    val compiledTemplate = TEMPLATE_ENGINE.getLiteralTemplate(template)
    compiledTemplate.evaluate(writer, this)

    return writer.toString()
    /**val mr = JSON_PATH_REGEX.find(template) ?: return template

     if (mr.groupValues.isEmpty()) {
     return template
     }

     val nodes = mr.groupValues.drop(0).map { "$$it" }

     println(nodes)

     val pendingText = nodes.map {
     logger.debug { "Parsing path $it" }
     try {
     JsonPath.read<Any>(this, it)
     } catch (e: InvalidPathException) {
     logger.debug(e) { "invalid path $it" }
     "转换失败"
     }
     }

     var result = template

     nodes.forEachIndexed { i, s ->
     result = result.replace(s, pendingText[i].toString())
     }

     return result*/
}
