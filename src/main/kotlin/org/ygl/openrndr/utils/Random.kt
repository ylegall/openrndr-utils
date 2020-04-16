package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun randomDouble(until: Number = 1.0): Double {
    return Random.nextDouble(until.toDouble())
}

fun randomDouble(low: Number = 0.0, high: Number = 1.0): Double {
    return Random.nextDouble(low.toDouble(), high.toDouble())
}

fun randomFloat(until: Number): Float {
    return Random.nextFloat() * until.toFloat()
}

fun randomFloat(from: Number, until: Number): Float {
    val start = from.toFloat()
    val range = until.toFloat() - start
    return start + Random.nextFloat() * range
}

fun Circle.randomPoint(random: Random = Random.Default): Vector2 {
    val randomAngle = random.nextDouble(2 * PI)
    val randomRadius = random.nextDouble(radius)
    return Vector2(
            center.x + randomRadius * cos(randomAngle),
            center.y + randomRadius * sin(randomAngle)
    )
}

fun Rectangle.randomPoint() = Vector2(
        Random.nextDouble(x, x + width),
        Random.nextDouble(y, y + height)
)

fun randomColor(r: Number? = null, g: Number? = null, b: Number? = null) = ColorRGBa(
        r?.toDouble() ?: Random.nextDouble(),
        g?.toDouble() ?: Random.nextDouble(),
        b?.toDouble() ?: Random.nextDouble()
)

fun randomColor(r: Number? = null, g: Number? = null, b: Number? = null, a: Number? = null) = ColorRGBa(
        r?.toDouble() ?: Random.nextDouble(),
        g?.toDouble() ?: Random.nextDouble(),
        b?.toDouble() ?: Random.nextDouble(),
        a?.toDouble() ?: Random.nextDouble()
)
