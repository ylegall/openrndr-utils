package org.ygl.openrndr.utils

import org.openrndr.draw.Drawer
import org.openrndr.shape.Rectangle

fun <T: Number> rect(x: T, y: T, width: T, height: T) = Rectangle(
        x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()
)

fun <T: Number> Drawer.rect(x: T, y: T, width: T, height: T) {
    this.rectangle(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}
