package org.ygl.openrndr.utils

import org.openrndr.math.Vector2


fun lerp(a: Double, b: Double, time: Double) = a * (1 - time) + b * time

fun mix(v0: Vector2, v1: Vector2, t: Double): Vector2 {
    return v0 * (1.0 - t) + v1 * t
}
