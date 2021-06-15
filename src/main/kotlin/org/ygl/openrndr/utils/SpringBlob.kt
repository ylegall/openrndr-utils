package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.math.mix


class SpringBlob(
    points: Iterable<Vector2>,
    val springStrength: Double,
    val springLength: Double,
    val damping: Double = 0.9
) {
    val points = points.toMutableList()
    val previousPoints = points.toMutableList()

    private fun index(index: Int) = (index + points.size) % points.size

    // force from p1 towards p2
    private fun springForce(p1: Vector2, p2: Vector2): Vector2 {
        val delta = p2 - p1
        val magnitude = delta.length / springLength
        return delta.normalized * magnitude
    }

    fun addForce(target: Vector2, magnitude: Double) {
        for (i in points.indices) {
            val delta = target - points[i]
            points[i] += (delta.normalized * magnitude)
        }
    }

    fun update(dt: Double) {
        val newPositions = points.toMutableList()
        for (i in points.indices) {
            val currentPosition = points[i]
            val lastPosition = previousPoints[i]
            val leftNeighbor = points[index(i + 1)]
            val rightNeihbor = points[index(i - 1)]

            val oldVelocity = (currentPosition - lastPosition).length / dt
            var newPosition = currentPosition + oldVelocity * damping * dt
            newPosition = newPosition + springForce(newPosition, leftNeighbor) + springForce(newPosition, rightNeihbor)
            newPositions[i] = newPosition
        }
        for (i in points.indices) {
            previousPoints[i] = points[i]
            points[i] = newPositions[i]
        }
    }
}