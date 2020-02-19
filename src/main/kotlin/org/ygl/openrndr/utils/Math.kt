package org.ygl.openrndr.utils



inline fun <reified T: Number> T.linearInterpolate(start: T, end: T, newStart: T, newEnd: T): Double {
    val startDouble = start.toDouble()
    val endDouble = end.toDouble()
    val position = this.toDouble().coerceIn(startDouble, endDouble)
    val progress = position / (endDouble - startDouble)
    return newStart.toDouble() + progress * (newEnd.toDouble() - newStart.toDouble())
}