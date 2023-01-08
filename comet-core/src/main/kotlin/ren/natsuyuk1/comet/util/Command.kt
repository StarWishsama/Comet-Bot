package ren.natsuyuk1.comet.util

import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.command.asMember
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.isOperator

internal val groupAdminChecker: suspend (CometUser, PlatformCommandSender) -> Boolean =
    { _, sender -> sender.asMember()?.isOperator() == true }
