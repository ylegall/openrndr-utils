package org.ygl.openrndr.utils

import org.openrndr.math.Vector2

data class MutableVector(
        var x: Double,
        var y: Double
) {

    constructor(x: Number, y: Number): this(x.toDouble(), y.toDouble())
    constructor(vector: MutableVector): this(vector.x, vector.y)
    constructor(vector2: Vector2): this(vector2.x, vector2.y)

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

    operator fun plusAssign(vector: MutableVector) {
        x += vector.x
        y += vector.y
    }

    operator fun plus(vector: MutableVector) = MutableVector(
            x + vector.x,
            y + vector.y
    )

    operator fun minusAssign(vector: MutableVector) {
        x -= vector.x
        y -= vector.y
    }

    operator fun minus(vector: MutableVector) = MutableVector(
            x - vector.x,
            y - vector.y
    )

    fun squaredLength() = x * x + y * y

    fun toVector2() = Vector2(x, y)

}

fun mvector(vector: Vector2) = MutableVector(vector.x, vector.y)

fun <T: Number> mvector(x: T, y: T) = MutableVector(x, y)