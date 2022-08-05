package ren.natsuyuk1.comet.utils.graphics

import java.awt.Graphics2D
import java.awt.RenderingHints

fun Graphics2D.option(): Graphics2D = run {
    setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    )
    setRenderingHint(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY
    )

    setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )

    this
}
