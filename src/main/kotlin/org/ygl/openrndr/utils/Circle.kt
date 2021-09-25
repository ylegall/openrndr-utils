package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


fun Circle.overlaps(other: Circle): Boolean {
    return this.center.distanceTo(other.center) < radius + other.radius
}

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

fun circleFromPoints(
    p1: Vector2,
    p2: Vector2,
    p3: Vector2
): Circle {
    val na = p1.squaredLength
    val nb = p2.squaredLength
    val nc = p3.squaredLength
    val dy = 2 * (p2.y - p1.y) * (p2.x - p3.x) - 2 * (p1.x - p2.x) * (p3.y - p2.y)
    val y = ((p1.x - p2.x) * (nb - nc) - (p2.x - p3.x) * (na - nb)) / dy
    val x = (na - nb + 2 * (p2.y - p1.y) * y) / (2 * (p1.x - p2.x))
    val dx = x - p1.x
    val dy2 = y - p1.y
    val r = sqrt(dx*dx + dy2*dy2)
    return Circle(x, y, r)
}
