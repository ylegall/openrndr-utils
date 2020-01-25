package org.ygl.openrndr.utils

import org.openrndr.math.Vector2

class MutableVector(x: Number, y: Number) {

    constructor(vector: MutableVector): this(vector.x, vector.y)
    constructor(vector2: Vector2): this(vector2.x, vector2.y)

    var x = x.toDouble()
    var y = y.toDouble()

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

    operator fun minusAssign(vector: MutableVector) {
        x -= vector.x
        y -= vector.y
    }

    fun toVector2() = Vector2(x, y)
}

fun <T: Number> mVector(x: T, y: T) = MutableVector(x, y)