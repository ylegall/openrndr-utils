package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import kotlin.math.sqrt


data class Complex(
        val real: Double,
        val imaginary: Double
) {
    constructor(point: Vector2): this(point.x, point.y)

    fun toVector2() = Vector2(real, imaginary)

    operator fun times(x: Double) = Complex(real * x, imaginary * x)

    operator fun times(c: Complex) = Complex(
            real * c.real - imaginary * c.imaginary,
            real * c.imaginary + c.real * imaginary
    )

    operator fun plus(c: Complex) = Complex(real + c.real, imaginary + c.imaginary)

    operator fun plus(x: Double) = Complex(real + x, imaginary)

    operator fun minus(c: Complex) = Complex(real - c.real, imaginary - c.imaginary)

    operator fun minus(x: Double) = Complex(real - x, imaginary)

    fun modulus() = sqrt(real * real + imaginary * imaginary)
}

fun complex(real: Number, imaginary: Number) = Complex(real.toDouble(), imaginary.toDouble())

operator fun Double.times(c: Complex) = Complex(c.real * this, c.imaginary * this)

fun sqrt(z: Complex): Complex {
    val r = z.modulus()
    val num = (z + r)
    val den = num.modulus()
    return num * (sqrt(r) / den)
}
