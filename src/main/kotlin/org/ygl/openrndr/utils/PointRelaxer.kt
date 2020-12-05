package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle


fun relaxPoints(
        points: MutableList<Vector2>,
        radius: Double,
        maxIterations: Int = -1,
        minOverlap: Double = 0.001
): Int {
    var iterations = 0
    val positionDeltas = MutableList(points.size) { Vector2.ZERO }

    while (iterations < maxIterations || maxIterations < 0) {
        val index = KDTree2.fromPoints(points.indices.toList()) { points[it] }
        var changed = false

        for (i in points.indices) {
            val p1 = points[i]
            val neighbors = index.queryRange(Rectangle.fromCenter(p1, 4 * radius, 4 * radius)).filter { it != i }
            var overlappingNeighbors = 0

            for (j in neighbors) {
                val p2 = points[j]
                val delta = p1 - p2
                val dist = delta.length
                val overlap = 2 * radius - dist
                if (overlap > minOverlap) {
                    overlappingNeighbors++
                    positionDeltas[i] += delta.normalized * (overlap / 2)
                }
            }

            if (overlappingNeighbors > 0) {
                changed = true
                positionDeltas[i] = positionDeltas[i] / overlappingNeighbors.toDouble()
            }
        }

        if (!changed) {
            break
        }

        for (i in points.indices) {
            points[i] += positionDeltas[i]
        }

        positionDeltas.fill(Vector2.ZERO)
        iterations++
    }
    return iterations
}

//fun relaxPoints(
//        points: List<Vector2>,
//        radii: List<Double>,
//        maxIterations: Int
//): MutableList<Vector2> {
//    var i = 0
//    val newPoints = MutableList(points.size) { Vector2.ZERO }
//
//    val maxRadius = radii.maxOrNull()!!
//    val index = KDTree2.fromPoints(points.indices.toList()) { points[it] }
//
//    while (i < maxIterations || maxIterations < 0) {
//        for (i in points.indices) {
//            val p1 = points[i]
//            val r1 = radii[i]
//
//            for (j in points.indices) {
//                if (i == j) continue
//                val p2 = points[j]
//                val r2 = radii[j]
//                val delta = (r1 + r2 - p1.distanceTo(p2)).coerceAtLeast(0.0)
//
//            }
//        }
//
//        i++
//    }
//    return newPoints
//}

