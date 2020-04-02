package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


fun hexCorner(
        center: Vector2,
        length: Double,
        i: Int
): Vector2 {
    val angle = i * (PI / 3) - PI / 6
    return Vector2(
            center.x + length * cos(angle),
            center.y + length * sin(angle)
    )
}

fun hexagon(
        x: Number,
        y: Number,
        length: Number,
        closed: Boolean = true
) = hexagon(vector2(x, y), length.toDouble(), closed)

fun hexagon(
        center: Vector2,
        length: Double,
        closed: Boolean = true
): ShapeContour {
    return contour {
        for (i in 0 until 6) {
            moveOrLineTo(hexCorner(center, length, i))
        }
        if (closed) close()
    }
}

fun getHexWidth(length: Number) = sqrt(3.0) * length.toDouble()

fun getHexHeight(length: Number) = 2.0 * length.toDouble()
