package ren.natsuyuk1.comet.utils.skiko

import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.TextStyle

fun ParagraphBuilder.addTextln() = addText("\n")

fun ParagraphBuilder.addTextln(text: String) = addText(text + "\n")

fun ParagraphBuilder.changeStyle(style: TextStyle?) = popStyle().pushStyle(style)
