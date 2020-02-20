package org.ygl.openrndr.utils


inline fun <reified T: Number> T.rangeMap(start: T, end: T, newStart: T, newEnd: T): Double {
    val startDouble = start.toDouble()
    val endDouble = end.toDouble()
    val newStartDouble = newStart.toDouble()
    val newEndDouble = newEnd.toDouble()
    return newStartDouble + (newEndDouble - newStartDouble) * ((this.toDouble() - startDouble) / (endDouble - startDouble))
}