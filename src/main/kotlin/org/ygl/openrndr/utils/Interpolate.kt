package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.math.smoothstep


fun lerp(a: Double, b: Double, time: Double) = a * (1 - time) + b * time

fun lerp(a: Float, b: Float, time: Float) = a * (1 - time) + b * time

fun mix(v0: Vector2, v1: Vector2, t: Double): Vector2 {
    return v0 * (1.0 - t) + v1 * t
}

fun smoothBell(t: Double) = smoothBell(0.0, 1.00, t)

fun smoothBell(left: Double = 0.0, right: Double = 1.0, t: Double): Double {
    val half = (left + right) / 2.0
    return if (t < half) {
        smoothstep(left, half, t)
    } else {
        smoothstep(left, half, left + (right - t))
    }
}
