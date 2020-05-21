package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa


fun <T: Number> color(r: T, g: T, b: T) = color(r, g, b, 255)

fun <T: Number> color(r: T, g: T, b: T, a: T) = ColorRGBa(
        r.toDouble() / 255,
        g.toDouble() / 255,
        b.toDouble() / 255,
        a.toDouble() / 255
)

