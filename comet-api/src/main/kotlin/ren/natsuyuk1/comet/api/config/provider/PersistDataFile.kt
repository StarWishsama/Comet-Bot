/*
 * Copyright (c) 2022- Sorapointa
 *
 * 此源代码的使用受 Apache-2.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the Apache-2.0 License which can be found through the following link.
 *
 * https://github.com/Sorapointa/Sorapointa/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.config.provider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

private val logger = mu.KotlinLogging.logger { }

open class PersistDataFile<T : Any>(
    final override val file: File,
    private val serializer: KSerializer<T>,
    default: T,
    override val format: StringFormat = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    },
    final override val scope: CoroutineScope =
        ModuleScope("PersistDataFile", dispatcher = Dispatchers.IO),
    final override val readOnly: Boolean = false,
) : PersistFile<T> {
    private val clazz = default::class

    private val mutex = Mutex()

    final override var data: T = default

    override suspend fun init(): Unit =
        withContext(scope.coroutineContext) {
            load()
        }

    suspend fun initAndLoad(): T =
        withContext(scope.coroutineContext) {
            load()
        }

    /**
     * Save [PersistDataFile]
     *
     * @param saveData saved data
     */
    override suspend fun save(saveData: T) = mutex.withLock {
        if (readOnly) {
            if (!file.exists()) {
                logger.debug { "正在初始化数据 $saveData" }
                file.touch()
                file.writeTextBuffered(format.encodeToString(serializer, saveData))
            }
            return@withLock
        }

        withContext(scope.coroutineContext) {
            logger.debug { "正在保存数据 $saveData" }
            file.touch()
            file.writeTextBuffered(format.encodeToString(serializer, saveData))
        }
    }

    /**
     * Load [PersistDataFile]
     */
    override suspend fun load(): T {
        return withContext(scope.coroutineContext) {
            if (!file.exists()) {
                logger.debug { " ${file.absolutePath} 不存在, 正在创建默认配置..." }
                save(data)
            }

            mutex.withLock {
                val content = file.readTextBuffered()
                val t = format.decodeFromString(serializer, content)

                // Update readonly file content in case of data class changed
                if (readOnly) {
                    val encoded = format.encodeToString(serializer, t)
                    if (content.length != encoded.length) {
                        file.writeTextBuffered(encoded)
                    }
                }

                data = t
                t.also {
                    logger.debug { "已加载持久化文件 ${it::class.simpleName}" }
                    logger.trace { "文件内容 $it" }
                }
            }
        }
    }
}
