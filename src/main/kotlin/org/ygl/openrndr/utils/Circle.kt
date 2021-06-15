package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.abs


fun Circle.reflectPoint(point: Vector2): Vector2 {
    val delta = point - center
    val length = delta.length
    val rr = radius * radius
    return Vector2(point.x * rr / length, point.y * rr / length)
}

fun Circle.reflectCircle(other: Circle): Circle {
    val delta = other.center - center
    val scaleFactor = (radius * radius) / (delta.squaredLength - other.radius * other.radius)
    return Circle(
        center.x + scaleFactor * delta.x,
        center.y + scaleFactor * delta.y,
        abs(scaleFactor) * other.radius
    )
}
