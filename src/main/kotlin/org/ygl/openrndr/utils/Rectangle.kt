package org.ygl.openrndr.utils

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment

fun <T: Number> rect(x: T, y: T, width: T, height: T) = Rectangle(
        x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()
)

fun <T: Number> Drawer.rect(x: T, y: T, width: T, height: T) {
    this.rectangle(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

fun Rectangle.left(): Segment {
    return Segment(corner, corner.copy(y = y + height))
}

fun Rectangle.right(): Segment {
    val start = Vector2(x + width, y)
    return Segment(start, start.copy(y = y + height))
}

fun Rectangle.top(): Segment {
    return Segment(corner, corner.copy(x = x + width))
}

fun Rectangle.bottom(): Segment {
    val start = Vector2(x, y + height)
    return Segment(corner, start.copy(x = start.x + width))
}
