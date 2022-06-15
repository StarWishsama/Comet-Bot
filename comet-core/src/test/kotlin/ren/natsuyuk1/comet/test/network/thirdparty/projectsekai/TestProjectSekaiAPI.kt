/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.test.network.thirdparty.projectsekai

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.test.network.client

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAPI {

    // 迷い子の手を引く、そのさきは
    private val eventID = 61

    // Welcome to add me as friend :D
    private val id = 210043933010767872

    @Test
    fun testEventProfileFetch() {
        runBlocking { client.getUserEventInfo(eventID, id) }
    }
}
