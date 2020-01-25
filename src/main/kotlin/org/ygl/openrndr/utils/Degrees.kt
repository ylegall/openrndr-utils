package org.ygl.openrndr.utils

inline class Degrees(
        val value: Double
) {
    fun toRadians() = Radians(value * 180.0 / Math.PI)
}

fun <T: Number> T.degrees(value: T) = Degrees(value.toDouble())