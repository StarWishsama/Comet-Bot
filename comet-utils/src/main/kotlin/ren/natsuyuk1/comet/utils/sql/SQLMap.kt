/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.utils.sql

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

abstract class MapTable<TID : Comparable<TID>, K, V>(
    tableName: String
) : IdTable<TID>(tableName) {

    abstract val key: Column<K>
    abstract val value: Column<V>
}

abstract class CacheableData<TID : Comparable<TID>>(
    protected val id: EntityID<TID>,
    private val table: IdTable<TID>
) {

    private var cache: Query = originalData
    private var needToUpdate = true

    protected val originalData: Query
        get() = run {
            if (needToUpdate) {
                needToUpdate = false
                cache = table.select { table.id eq id }
            }
            return cache
        }

    protected fun needToUpdate() {
        needToUpdate = true
    }
}

class SQLDatabaseMap<TID : Comparable<TID>, K, V>(
    id: EntityID<TID>,
    private val mapTable: MapTable<TID, K, V>
) : CacheableData<TID>(id, mapTable), MutableMap<K, V> {

    override val size: Int
        get() = originalData.fetchSize ?: 0
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = originalData.map { Entry(it[mapTable.key], it[mapTable.value]) }.toMutableSet()
    override val keys: MutableSet<K>
        get() = originalData.map { it[mapTable.key] }.toMutableSet()
    override val values: MutableCollection<V>
        get() = originalData.map { it[mapTable.value] }.toMutableSet()

    inner class Entry(
        override val key: K,
        override val value: V
    ) : MutableMap.MutableEntry<K, V> {

        override fun setValue(newValue: V): V {
            needToUpdate()
            mapTable.update({ mapTable.key eq key }) {
                it[mapTable.value] = newValue
            }
            return value
        }

        override fun toString(): String = "$key=$value"
    }

    override fun clear() {
        needToUpdate()
        mapTable.deleteWhere { mapTable.id eq id }
    }

    override fun isEmpty(): Boolean =
        originalData.empty()

    override fun remove(key: K): V? {
        needToUpdate()
        val previous = entries.firstOrNull { it.key == key }
        mapTable.deleteWhere { mapTable.key eq key }
        return previous?.value
    }

    override fun putAll(from: Map<out K, V>) {
        needToUpdate()
        mapTable.batchInsert(from.entries) { entry ->
            this[mapTable.id] = id
            this[mapTable.key] = entry.key
            this[mapTable.value] = entry.value
        }
    }

    override fun put(key: K, value: V): V? {
        needToUpdate()
        val previous = entries.firstOrNull { it.key == key }
        previous?.also {
            mapTable.update({ mapTable.key eq key }) {
                it[mapTable.value] = value
            }
        } ?: run {
            mapTable.insert {
                it[mapTable.id] = id
                it[mapTable.key] = key
                it[mapTable.value] = value
            }
        }
        return previous?.value
    }

    override fun get(key: K): V? =
        entries.firstOrNull { it.key == key }?.value

    override fun containsValue(value: V): Boolean =
        values.firstOrNull { it == value }?.let { true } ?: false

    override fun containsKey(key: K): Boolean =
        keys.firstOrNull { it == key }?.let { true } ?: false

    override fun toString(): String =
        "SQLMap[tableName=${mapTable.tableName},id=${id.value}," +
            "value=${entries.joinToString(prefix = "[", postfix = "]")}]"
}
