package ren.natsuyuk1.comet.objects.config

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import moe.sdl.ipdb.Reader
import mu.KotlinLogging
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File
import java.lang.ref.SoftReference

private val logger = KotlinLogging.logger {}

object IpdbConfig : PersistDataFile<IpdbConfig.Data>(
    File(configDirectory, "ipdb.yml"), Data.serializer(), Data(), Yaml(),
    readOnly = true
) {
    @Serializable
    data class Data(
        @Comment("是否开启 IP 服务") val enable: Boolean = true,
        @Comment("IPDB v4 路径") @SerialName("pathV4") private val _pathV4: String? = null,
        @Comment("IPDB v6 路径") @SerialName("pathV6") private val _pathV6: String? = null,
    ) {
        private fun checkFile(path: String?): File? {
            val file = File(path ?: return null)
            if (!file.exists()) {
                throw NoSuchFileException(file)
            }
            if (!file.isFile) {
                throw FileSystemException(file, reason = "is not a file")
            }
            if (!file.extension.equals("ipdb", ignoreCase = true)) {
                throw FileSystemException(file, reason = "expected a \".ipdb\" file but actual: ${file.extension}")
            }
            return file
        }

        @Transient
        val pathV4 = checkFile(_pathV4)

        @Transient
        val pathV6 = checkFile(_pathV6)

        private fun db(path: File?) = ConcRef {
            val file = path ?: return@ConcRef null
            logger.info { "Loading IPDB from $path" }
            Reader(file)
        }

        @Transient
        val dbV4 = db(pathV4)

        @Transient
        val dbV6 = db(pathV6)
    }
}

class ConcRef(
    private val load: suspend () -> Reader?
) {
    @Volatile
    private var value: SoftReference<Reader?> = SoftReference(null)
    private val lock = Mutex()

    suspend fun get(): Reader? {
        if (value.get() == null) {
            lock.withLock {
                if (value.get() == null) {
                    value = SoftReference(load())
                }
            }
        }
        return value.get()
    }

    override fun toString(): String {
        return "ConcRef(value=$value)"
    }
}
