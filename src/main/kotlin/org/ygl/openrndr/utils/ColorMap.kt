package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import kotlin.math.ceil


class ColorMap(
        private val colorIntervals: List<ColorRGBa>
) {
    init {
        check(colorIntervals.isNotEmpty()) { "colorIntervals must not be empty" }
    }

    private val intervalSize = 1.0 / (colorIntervals.size - 1)

    operator fun get(percent: Double): ColorRGBa {
        return when (percent.coerceIn(0.0, 1.0)) {
            0.0 -> colorIntervals.first()
            1.0 -> colorIntervals.last()
            else -> {
                val quotient = percent / intervalSize
                val low = quotient.toInt()
                val high = ceil(quotient).toInt()
                return mix(colorIntervals[low], colorIntervals[high], (percent % intervalSize)/intervalSize)
            }
        }
    }
}
