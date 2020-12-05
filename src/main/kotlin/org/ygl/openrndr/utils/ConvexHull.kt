package org.ygl.openrndr.utils

import org.openrndr.math.Vector2


fun convexHull(inputPoints: List<Vector2>): List<Vector2> {

    fun orientation(p: Vector2, q: Vector2, r: Vector2): Int {
        val value = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        return when {
            value == 0.0 -> 0
            value > 0    -> 1
            else         -> 2
        }
    }

    val n = inputPoints.size
    require(n > 3) { "must have at least 3 input points" }

    val hull = mutableListOf<Vector2>()
    val leftmostIndex = inputPoints.indices.minBy { inputPoints[it].x }!!

    var p = leftmostIndex
    var q: Int
    do {
        hull.add(inputPoints[p])
        q = (p + 1) % n

        for (i in 0 until n) {
            if (orientation(inputPoints[p], inputPoints[i], inputPoints[q]) == 2) {
                q = i
            }
        }
        p = q
    } while (p != leftmostIndex)
    return hull
}