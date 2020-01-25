package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa

fun randomColorRGBa(
        r: Double? = null,
        g: Double? = null,
        b: Double? = null,
        a: Double? = null
) = ColorRGBa(
        r ?: randomDoubleColor(),
        g ?: randomDoubleColor(),
        b ?: randomDoubleColor(),
        a ?: randomDoubleColor()
)

private fun randomIntChannel() = (0..255).random()
private fun randomDoubleColor() = (0..255).random() / 255.0

fun Int.argb2rgba() = ColorRGBa(
        r = ((this shr 16) and 0xFF) / 255.0,
        g = ((this shr 8) and 0xFF) / 255.0,
        b = (this and 0xFF) / 255.0,
        a = ((this shr 24) and 0xFF) / 255.0
)

fun <T: Number> color(r: T, g: T, b: T) = color(r, g, b, 255)

fun <T: Number> color(r: T, g: T, b: T, a: T) = ColorRGBa(
        r.toDouble() / 255,
        g.toDouble() / 255,
        b.toDouble() / 255,
        a.toDouble() / 255
)