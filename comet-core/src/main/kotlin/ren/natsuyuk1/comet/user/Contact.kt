/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.user

import kotlinx.serialization.Serializable

/**
 * [Contact] 联系人, 是所有可聊天对象的父类
 */
@Serializable
abstract class Contact {
    /**
     * 可以是用户或群聊
     */
    abstract val id: Long
}

/**
 * [User] 用户
 *
 */
@Serializable
abstract class User : Contact() {
    abstract override val id: Long

    /**
     * 备注信息
     *
     * 当该用户与机器人存在好友关系时才有备注，否则为空
     */
    abstract val remark: String
}

/**
 * [Group] 群组
 */
@Serializable
abstract class Group : Contact() {

    abstract override val id: Long

    /**
     * 群名称
     *
     * 修改请使用 Group.setGroupName(groupName)
     */
    abstract val name: String
}
