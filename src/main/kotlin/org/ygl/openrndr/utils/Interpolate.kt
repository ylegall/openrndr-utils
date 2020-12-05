package org.ygl.openrndr.utils

import org.openrndr.math.smoothstep
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

// https://www.iquilezles.org/www/articles/functions/functions.htm

fun lerp(a: Double, b: Double, time: Double) = a * (1 - time) + b * time

fun lerp(a: Float, b: Float, time: Float) = a * (1 - time) + b * time

fun smoothBell(t: Double) = smoothBell(0.0, 1.00, t)

fun smoothBell(left: Double = 0.0, right: Double = 1.0, t: Double): Double {
    val half = (left + right) / 2.0
    return if (t < half) {
        smoothstep(left, half, t)
    } else {
        smoothstep(left, half, left + (right - t))
    }
}

fun cubicPulse(center: Double, halfwidth: Double, x: Double): Double {
    var x1 = abs(x - center)
    if (x1 > halfwidth) return 0.0
    x1 /= halfwidth
    return 1.0 - x1 * x1 * (3.0 - 2.0 * x1)
}

fun expImpulse(x: Double, k: Double): Double {
    val h = k * x
    return h * exp(1 - h)
}

fun parabola(x: Double, k: Double): Double {
    return (4.0 * x * (1.0 - x)).pow(k)
}

fun Double.wrapIn(range: ClosedRange<Double>): Double {
    return wrapIn(range.start, range.endInclusive)
}

fun Double.wrapIn(start: Double, stop: Double): Double {
    val rangeSize = stop - start
    return when {
        this < start -> stop - ((start - this) % rangeSize)
        this >= stop -> start + ((this - stop) % rangeSize)
        else         -> this
    }
}
