package ren.natsuyuk1.comet.network.thirdparty.bilibili.user

import moe.sdl.yabapi.data.info.Official
import moe.sdl.yabapi.data.info.OfficialRole
import moe.sdl.yabapi.data.info.UserVip
import moe.sdl.yabapi.data.info.VipStatus

fun UserVip.asReadable(): String =
    buildString {
        if (status == VipStatus.NORMAL) {
            append(label?.text ?: "")
        }
    }

fun Official.asReadable(): String =
    buildString {
        if (isCertified.value) {
            when (role) {
                OfficialRole.PERSONAL -> append("个人认证 > $title")
                OfficialRole.ORGANIZATION -> append("机构认证 > $title")
                else -> {}
            }
        }
    }
