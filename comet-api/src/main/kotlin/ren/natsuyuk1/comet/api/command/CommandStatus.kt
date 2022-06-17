/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

/**
 * [CommandStatus]
 *
 * 命令状态
 *
 * @param name 状态
 * @param pass 是否可视作成功执行
 */
sealed class CommandStatus(val name: String, private val pass: Boolean) {
    class Success : CommandStatus("成功", true)
    class Error : CommandStatus("异常", false)
    class NoPermission : CommandStatus("无权限", true)
    class Failed : CommandStatus("失败", true)
    class Disabled : CommandStatus("命令被禁用", true)
    class PassToSession : CommandStatus("移交会话处理", false)
    class NotACommand : CommandStatus("非命令", false)
    class CometIsClose : CommandStatus("Comet 已关闭", false)
    class ValidateFailed : CommandStatus("冷却/无硬币", true)

    fun isPassed(): Boolean = this.pass
}
