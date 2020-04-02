package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import java.lang.StringBuilder


class LSystem(
        val angle: Double,
        val rules: Map<Char, String>
) {
    fun getNextStates(actions: CharSequence): String {
        val result = StringBuilder()
        for (action in actions) {
            when (action) {
                in rules -> result.append(rules[action])
                else -> result.append(action)
            }
        }
        return result.toString()
    }

    fun getPathPoints(
            actions: String,
            start: Vector2,
            length: Double
    ): List<Vector2> {
        val points = mutableListOf<Vector2>()
        var cursor = start
        var currentAngle = 0.0
        val vec = Vector2.UNIT_X * length
        points.add(start)
        for (action in actions) {
            when (action) {
                '-'  -> currentAngle += angle
                '+'  -> currentAngle -= angle
                else -> {
                    cursor += vec.rotate(currentAngle.radians())
                    points.add(cursor)
                }
            }
        }
        return points
    }
}
