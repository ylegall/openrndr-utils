package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.color.rgb
import kotlin.math.ceil
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

    private val intervalSize = 1.0 / (colorIntervals.size - 1)

    fun random() = get(Random.nextDouble())

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
