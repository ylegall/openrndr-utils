package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa


fun <T: Number> color(r: T, g: T, b: T) = color(r, g, b, 255)

fun <T: Number> color(r: T, g: T, b: T, a: T) = ColorRGBa(
        r.toDouble() / 255,
        g.toDouble() / 255,
        b.toDouble() / 255,
        a.toDouble() / 255
)

fun ColorRGBa.grayscaleLinear(): Double {
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}