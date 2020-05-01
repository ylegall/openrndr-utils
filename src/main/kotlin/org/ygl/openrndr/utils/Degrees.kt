package org.ygl.openrndr.utils

inline class Degrees(
        val value: Double
) {
    fun toRadians() = Radians(value * Math.PI / 180.0)
}

fun <T: Number> T.degrees() = Degrees(this.toDouble())
