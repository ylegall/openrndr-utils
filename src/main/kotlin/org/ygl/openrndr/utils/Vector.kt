package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun <T: Number> vector2(x: T, y: T) = Vector2(x.toDouble(), y.toDouble())

fun Vector2.rotate(degrees: Degrees) = rotate(degrees.toRadians())

fun Vector2.rotate(amount: Radians): Vector2 {
    val rads = amount.value
    return Vector2(
            x * cos(rads) - y * sin(rads),
            x * sin(rads) + y * cos(rads)
    )
}

fun <T: Number> Vector2.distanceFrom(x: T, y: T): Double {
    return sqrt((this.x - x.toDouble()).pow(2) + (this.y - y.toDouble()).pow(2))
}