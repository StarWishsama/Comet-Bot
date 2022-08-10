/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.test.network.thirdparty.projectsekai

import io.ktor.client.features.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.network.client
import ren.natsuyuk1.comet.test.print
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertTrue

private val logger = mu.KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAPI {
    @BeforeAll
    fun init() {
        if (isCI()) {
            return
        }

        initTestDatabase()
        DatabaseManager.loadTables(ProjectSekaiDataTable, ProjectSekaiUserDataTable)
        runBlocking {
            ProjectSekaiManager.init(EmptyCoroutineContext)
            ProjectSekaiData.updateData()
        }
    }

    // Represent to event named 迷い子の手を引く、そのさきは
    private val eventID = 61

    // Welcome to add me as friend :D
    private val id = 210043933010767872L

    @Test
    fun testEventProfileFetch() {
        if (isCI()) {
            return
        }

        runBlocking {
            val user = CometUser.create(id)

            ProjectSekaiUserData.createData(user.id.value, this@TestProjectSekaiAPI.id)

            val data = transaction {
                ProjectSekaiData.all().forEach {
                    it.print()
                }
                ProjectSekaiUserData.getUserPJSKData(user.id.value)
            } ?: return@runBlocking

            client.getUserEventInfo(eventID, id)
                .also { it.toMessageWrapper(data, eventID).print() }

            transaction {
                ProjectSekaiUserDataTable.deleteAll()
                UserTable.deleteAll()
            }
        }
    }

    @Test
    fun testEventRankingPositionFetch() {
        if (isCI()) {
            return
        }

        runBlocking { client.getSpecificRankInfo(eventID, 10000) }
    }

    @Test
    fun testEventListFetch() {
        if (isCI()) {
            return
        }

        runBlocking { println(client.getEventList()) }
    }

    @Test
    fun testRankPredictionFetch() {
        if (isCI()) {
            return
        }

        runBlocking {
            try {
                client.getRankPredictionInfo().toMessageWrapper().print()
            } catch (ignored: ServerResponseException) {
            }
        }
    }

    @Test
    fun testUserInfoFetch() {
        if (isCI()) {
            return
        }

        runBlocking {
            client.getUserInfo(id).toMessageWrapper().print()
        }
    }

    @Test
    fun testB30() {
        runBlocking {
            val b30 = client.getUserInfo(id).getBest30Songs()
            Json.encodeToString(b30).print()

            assertTrue { b30.size == 30 }
        }
    }
}
