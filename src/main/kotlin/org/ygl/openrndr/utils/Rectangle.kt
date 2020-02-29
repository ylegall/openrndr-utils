package org.ygl.openrndr.utils

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.random.Random

fun <T: Number> rect(x: T, y: T, width: T, height: T) = Rectangle(
        x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()
)

fun <T: Number> Drawer.rect(x: T, y: T, width: T, height: T) {
    this.rectangle(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

fun Rectangle.randomPoint() = Vector2(
        Random.nextDouble(x, x + width),
        Random.nextDouble(y, y + height)
)
