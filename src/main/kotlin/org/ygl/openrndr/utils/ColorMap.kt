package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.color.rgb
import kotlin.random.Random


class ColorMap(
        val colorIntervals: List<ColorRGBa>
) {
    constructor(
            hexStrings: Iterable<String>,
            opacity: Double = 1.0
    ): this(hexStrings.map { rgb(it).opacify(opacity) })

    init {
        check(colorIntervals.isNotEmpty()) { "colorIntervals must not be empty" }
    }

    val size; get() = colorIntervals.size

    fun random(random: Random = Random) = get(random.nextDouble())

    operator fun get(percent: Double): ColorRGBa {
        return when {
            percent <= 0.0 -> colorIntervals.first()
            percent >= 1.0 -> colorIntervals.last()
            else -> {
                val scaledPercent = (colorIntervals.size - 1) * percent
                val low = (scaledPercent).toInt()
                val high = low + 1
                return mix(colorIntervals[low], colorIntervals[high], scaledPercent % 1.0)
            }
        }
    }
}
