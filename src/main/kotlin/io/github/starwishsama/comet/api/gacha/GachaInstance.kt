/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.gacha

import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.custom.CustomPool
import io.github.starwishsama.comet.objects.gacha.pool.GachaPool

abstract class GachaInstance(val name: String) {

    /**
     * 检查是否可以使用
     */
    abstract fun isUsable()

    /**
     * 检查抽卡所需文件是否需要更新
     */
    abstract fun checkUpdate(): Boolean

    /**
     * 下载抽卡文件
     */
    abstract fun downloadFile()

    /**
     * 将抽卡结果解析为文本
     */
    abstract fun parseGachaResult(result: GachaResult): String

    /**
     * 解析自定义卡池文件到对应游戏卡池类
     */
    abstract fun parseCustomPool(customPool: CustomPool): GachaPool
}