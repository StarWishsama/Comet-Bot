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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getSpecificRankInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiHelper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.toMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.network.client
import ren.natsuyuk1.comet.test.print

private val logger = mu.KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAPI {
    @BeforeAll
    fun init() {
        initTestDatabase()
        DatabaseManager.loadTables(ProjectSekaiDataTable, ProjectSekaiUserDataTable)
        runBlocking { ProjectSekaiData.updateData() }
        ProjectSekaiHelper.refreshCache()
    }

    // Represent to event named 迷い子の手を引く、そのさきは
    private val eventID = 61

    // Welcome to add me as friend :D
    private val id = 210043933010767872

    @Test
    fun testEventProfileFetch() {
        runBlocking {
            client.getUserEventInfo(eventID, id)
                .also { it.toMessageWrapper(eventID).apply { logger.debug { this.parseToString() } } }
        }
    }

    @Test
    fun testEventRankingPositionFetch() {
        runBlocking { client.getSpecificRankInfo(eventID, 10000) }
    }

    @Test
    fun testEventListFetch() {
        runBlocking { println(client.getEventList()) }
    }

    @Test
    fun testRankPredictionFetch() {
        runBlocking {
            try {
                client.getRankPredictionInfo().toMessageWrapper().print()
            } catch (ignored: ServerResponseException) {
            }
        }
    }
}
