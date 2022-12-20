package ren.natsuyuk1.comet.utils.json

import com.jayway.jsonpath.DocumentContext
import io.pebbletemplates.pebble.PebbleEngine
import java.io.StringWriter

private val TEMPLATE_ENGINE: PebbleEngine = PebbleEngine.Builder().build()

class JsonPathMap(private val compiledPath: DocumentContext) : MutableMap<String?, Any?> {
    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsKey(key: String?): Boolean {
        return compiledPath.read(key, Any::class.java) == null
    }

    override fun containsValue(value: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override operator fun get(key: String?): Any? {
        return compiledPath.read(key)
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

fun JsonPathMap.formatByTemplate(template: String): String {
    val writer = StringWriter()
    val compiledTemplate = TEMPLATE_ENGINE.getLiteralTemplate(template)
    compiledTemplate.evaluate(writer, this)

    return writer.toString()
}
