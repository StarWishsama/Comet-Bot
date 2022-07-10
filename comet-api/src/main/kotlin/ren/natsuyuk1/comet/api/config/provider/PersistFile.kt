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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

interface PersistFile<T : Any> {

    /**
     * Stored data
     */
    val data: T?

    /**
     * The location where data stores
     */
    val file: File

    val scope: CoroutineScope

    /**
     * Determined how to (de)serialize data for load/save
     */
    val format: StringFormat

    /**
     * Initialize file persist
     * Must be called at program start
     */
    suspend fun init()

    /**
     * Load data from disk storage,
     * assign value to [data], then return it
     */
    suspend fun load(): T

    /**
     * Save data to disk
     *
     * Should be invoked after change, or data will be lost
     *
     */
    suspend fun save(saveData: T = data!!)
}

internal fun KClass<*>.requireSerializable() {
    require(hasAnnotation<Serializable>() || hasAnnotation<Contextual>()) { "Class $qualifiedName is not @Serializable or @Contextual" }
}
