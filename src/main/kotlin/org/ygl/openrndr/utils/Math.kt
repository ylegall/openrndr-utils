package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.sqrt


inline fun <reified T: Number> T.rangeMap(start: T, end: T, newStart: T, newEnd: T): Double {
    val startDouble = start.toDouble()
    val endDouble = end.toDouble()
    val newStartDouble = newStart.toDouble()
    val newEndDouble = newEnd.toDouble()
    return newStartDouble + (newEndDouble - newStartDouble) * ((this.toDouble() - startDouble) / (endDouble - startDouble))
}

fun circleFrom3Points(p1: Vector2, p2: Vector2, p3: Vector2): Circle {
    val dot1 = p1.dot(p1)
    val dot2 = p2.dot(p2)
    val dot3 = p3.dot(p3)
    val a = p1.x * (p2.y - p3.y) - p1.y * (p2.x - p3.x) + p2.x * p3.y - p3.x * p2.y
    val b = dot1 * (p3.y - p2.y) + dot2 * (p1.y - p3.y) + dot3 * (p2.y - p1.y)
    val c = dot1 * (p2.x - p3.x) + dot2 * (p3.x - p1.x) + dot3 * (p1.x - p2.x)
    val d = dot1 * (p3.x * p2.y - p2.x * p3.y) +
            dot2 * (p1.x * p3.y - p3.x * p1.y) +
            dot3 * (p2.x * p1.y - p1.x * p2.y)
    val aa = 2 * a
    val center = Vector2(-b / aa, -c / aa)
    val radius = sqrt((b * b + c *c - 4 * a * d) / (4 * a * a))
    return Circle(center,  radius)
}
