package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class MutableVector2(
        var x: Double = 0.0,
        var y: Double = 0.0
) {

    constructor(x: Number, y: Number): this(x.toDouble(), y.toDouble())
    constructor(vector: MutableVector2): this(vector.x, vector.y)
    constructor(vector2: Vector2): this(vector2.x, vector2.y)

    fun set(x: Number, y: Number) {
        this.x = x.toDouble()
        this.y = y.toDouble()
    }

    operator fun <T: Number> timesAssign(scalar: T) {
        val doubleValue = scalar.toDouble()
        x *= doubleValue
        y *= doubleValue
    }

    operator fun <T: Number> divAssign(scalar: T) {
        val doubleValue = scalar.toDouble()
        x /= doubleValue
        y /= doubleValue
    }

    operator fun plusAssign(vector: MutableVector2) {
        x += vector.x
        y += vector.y
    }

    operator fun plus(vector: MutableVector2) = MutableVector2(
            x + vector.x,
            y + vector.y
    )

    operator fun minusAssign(vector: MutableVector2) {
        x -= vector.x
        y -= vector.y
    }

    operator fun minusAssign(vector: Vector2) {
        x -= vector.x
        y -= vector.y
    }

    operator fun minus(vector: MutableVector2) = MutableVector2(
            x - vector.x,
            y - vector.y
    )

    fun normalize() {
        val length = sqrt(squaredLength())
        x /= length
        y /= length
    }

    fun rotate(degrees: Degrees) {
        rotate(degrees.toRadians())
    }

    fun rotate(radians: Radians) {
        val rads = radians.value
        x = x * cos(rads) - y * sin(rads)
        y = x * sin(rads) + y * cos(rads)
    }

    fun squaredLength() = x * x + y * y

    fun distanceFrom(x: Number, y: Number): Double {
        val dx = this.x - x.toDouble()
        val dy = this.y - y.toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    fun toVector2() = Vector2(x, y)
}

fun <T: Number> mvector(x: T, y: T) = MutableVector2(x, y)

fun Vector2.toMutableVector2() = MutableVector2(this.x, this.y)
