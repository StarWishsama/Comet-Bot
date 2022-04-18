package io.github.starwishsama.comet.genshin.gacha.pipeline.env

import io.github.starwishsama.comet.genshin.utils.FileUtils
import io.github.starwishsama.comet.genshin.utils.JsonHelper
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.safeCast
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@kotlinx.serialization.Serializable
sealed class PipeEnvironment : PipeEnvironmentImpl {

    @Transient
    private var uid: Long = -1L
    @Transient
    private lateinit var pipeEnvironmentCache: PipeEnvironmentCache
    @Transient
    private lateinit var identifier: String
    @Transient
    override var parentEnvId: String? = null

    override fun init(uid: Long, identifier: String, cache: PipeEnvironmentCache, parentEnvId: String?) {
        this.uid = uid
        this.identifier = identifier
        this.pipeEnvironmentCache = cache
        this.parentEnvId = parentEnvId
    }

    override fun getParentEnvironment(): PipeEnvironmentImpl? = safeFromUID(uid, parentEnvId)

    override fun reset() {
        resetVariables()
        save()
    }

    abstract fun resetVariables()

    override fun save() {
        pipeEnvironmentCache.saveWithEnvironmentUpdate(identifier, this)
    }

    companion object {
        inline fun <reified T : PipeEnvironment> fromUID(uid: Long, identifier: String): T {
            return PipeEnvironmentCache.fromUID(uid).getEnvironment(identifier, T::class.java.constructors[0].newInstance().cast()).cast()
        }

        inline fun <reified T : PipeEnvironment> safeFromUID(uid: Long, identifier: String?): T? {
            return PipeEnvironmentCache.fromUID(uid).getUnsafeEnvironment(identifier).safeCast()
        }
    }

}

@kotlinx.serialization.Serializable
class PipeEnvironmentCache(private val uid: Long) {

    private val environmentCache: MutableMap<String, PipeEnvironment> = ConcurrentHashMap()

    fun getAllEnvironment(): Map<String, PipeEnvironment> = environmentCache

    fun getEnvironment(identifier: String, defaultValue: PipeEnvironment): PipeEnvironment {
        return environmentCache.getOrPut(identifier) {
            addEnvironment(uid, identifier, defaultValue)
            defaultValue
        }
    }

    fun getUnsafeEnvironment(identifier: String?): PipeEnvironment? = environmentCache[identifier]

    fun addEnvironment(uid: Long, identifier: String, environment: PipeEnvironment, parentEnvId: String? = null) {
        if (!environmentCache.containsKey(identifier)) {
            environment.init(uid, identifier, this, parentEnvId)
            environmentCache[identifier] = environment
            save()
        }
        parentEnvId?.let {
            environmentCache[identifier]?.let { env ->
                env.parentEnvId = it
            }
            save()
        }
    }

    fun reset() {
        environmentCache.forEach { (_, env) -> env.reset() }
    }

    fun saveWithEnvironmentUpdate(identifier: String, environment: PipeEnvironment) {
        environmentCache[identifier] = environment
        save()
    }

    fun save() {
        cacheList[uid] = this
        val gachaCache = getFile(uid)
        val cache = JsonHelper.json.encodeToString(this)
        gachaCache.writeText(cache)
    }

    companion object {

        private val cacheFile: ConcurrentHashMap<Long, File> = ConcurrentHashMap()
        private val cacheList: ConcurrentHashMap<Long, PipeEnvironmentCache> = ConcurrentHashMap()

        fun fromJson(json: String): PipeEnvironmentCache = JsonHelper.json.decodeFromString(json)

        fun fromUID(uid: Long): PipeEnvironmentCache {
            return cacheList.getOrPut(uid) {
                val cache = fromJson(getFile(uid).readText())
                cache.getAllEnvironment().forEach { (id, env) -> env.init(uid, id, cache) }
                cache
            }
        }

        fun getFile(uid: Long): File {
            return cacheFile.getOrPut(uid) {
                val gachaCache = FileUtils.getGachaCache(uid)
                if (gachaCache.exists()) {
                    gachaCache
                } else {
                    val cache = PipeEnvironmentCache(uid)
                    gachaCache.parentFile?.mkdirs()
                    gachaCache.createNewFile()
                    gachaCache.writeText(JsonHelper.json.encodeToString(cache))
                    gachaCache
                }
            }
        }
    }

}

interface PipeEnvironmentImpl {

    var parentEnvId: String?

    fun getParentEnvironment(): PipeEnvironmentImpl?

    fun init(uid: Long, identifier: String, cache: PipeEnvironmentCache, parentEnvId: String? = null)

    fun reset()

    fun save()

}