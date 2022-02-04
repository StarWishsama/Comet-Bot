/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili

import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import moe.sdl.yabapi.api.searchByType
import moe.sdl.yabapi.data.search.SearchNormalData
import moe.sdl.yabapi.data.search.SearchOption
import moe.sdl.yabapi.enums.search.SearchType

object SearchApi : ApiExecutor {

    suspend fun searchUser(keyword: String, searchType: SearchOption = SearchOption(SearchType.USER)): SearchNormalData? {
        return client.searchByType(keyword, searchType).data
    }

    override var usedTime: Int = 0
    override val duration: Int = 3

    override fun getLimitTime(): Int = 3000
}