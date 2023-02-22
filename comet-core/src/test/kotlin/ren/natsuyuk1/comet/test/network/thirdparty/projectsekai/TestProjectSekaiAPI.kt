/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.test.network.thirdparty.projectsekai

import io.ktor.client.plugins.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.toMessageWrapper
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.network.client
import ren.natsuyuk1.comet.test.print
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test

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
            ProjectSekaiData.updateEventInfo(true)
        }
    }

    // Represent to event named `Find a way out`
    private val eventID = 79

    // Welcome to add me as friend :D
    private val id = 210043933010767872L

    @Test
    fun testEventListFetch() {
        if (isCI()) {
            return
        }

        runBlocking { println(client.getEventList()) }
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

    @AfterAll
    fun cleanup() {
        DatabaseManager.close()
    }
}
