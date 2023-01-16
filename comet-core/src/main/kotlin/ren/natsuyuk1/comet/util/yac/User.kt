package ren.natsuyuk1.comet.util.yac

import moe.sdl.yac.core.BadParameterValue
import moe.sdl.yac.core.Context
import moe.sdl.yac.parameters.arguments.RawArgument
import moe.sdl.yac.parameters.arguments.convert
import moe.sdl.yac.parameters.options.RawOption
import moe.sdl.yac.parameters.options.convert
import ren.natsuyuk1.comet.api.message.AtElement
import ren.natsuyuk1.comet.api.message.MessageWrapper

internal fun valueToAt(context: Context, it: String, wrapper: MessageWrapper): Long {
    return if (it.startsWith("@")) {
        wrapper.find<AtElement>()?.target
    } else {
        it.toLongOrNull()
    } ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to a `Long` */
fun RawArgument.user(wrapper: MessageWrapper) = convert { valueToAt(context, it, wrapper) }

/** Convert the option values to a `Long` */
fun RawOption.user(wrapper: MessageWrapper) = convert({ localization.intMetavar() }) { valueToAt(context, it, wrapper) }
