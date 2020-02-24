package org.ygl.openrndr.utils

inline class Degrees(
        val value: Double
) {
    fun toRadians() = Radians(value * Math.PI / 180.0)
}

val <T: Number> T.degrees: Degrees; get() = Degrees(this.toDouble())