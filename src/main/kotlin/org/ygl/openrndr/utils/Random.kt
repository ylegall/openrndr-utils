package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.math.Polar
import org.openrndr.math.Spherical
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.mix
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
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

fun Random.nextFloat(until: Number = 1f): Float {
    return nextFloat() * until.toFloat()
}

fun Random.nextFloat(from: Number, until: Number): Float {
    val start = from.toFloat()
    val range = until.toFloat() - start
    return start + nextFloat() * range
}

fun Circle.randomPoint(random: Random = Random.Default): Vector2 {
    val randomAngle = random.nextDouble(2 * PI)
    val randomRadius = random.nextDouble(radius)
    return Vector2(
            center.x + randomRadius * cos(randomAngle),
            center.y + randomRadius * sin(randomAngle)
    )
}

fun Rectangle.randomPoint(random: Random = Random) = Vector2(
        random.nextDouble(x, x + width),
        random.nextDouble(y, y + height)
)

fun Segment.randomPoint(random: Random = Random) = mix(start, end, random.nextDouble())

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

fun randomSphereSurfacePoint(
        center: Vector3 = Vector3.ZERO,
        radius: Double = 1.0,
        random: Random = Random.Default
) = Vector3.fromSpherical(
        Spherical(random.nextDouble(360.0), random.nextDouble(180.0), radius)
) + center

fun randomSpherePoint(
        center: Vector3 = Vector3.ZERO,
        radius: Double = 1.0,
        random: Random = Random.Default
) = Vector3.fromSpherical(
        Spherical(random.nextDouble(360.0), random.nextDouble(180.0), random.nextDouble(radius))
) + center

fun randomUnitVector(random: Random = Random) = Vector2.fromPolar(Polar(360 * random.nextDouble(), 1.0))
