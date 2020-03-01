package org.ygl.openrndr.utils

import org.openrndr.math.Vector2

interface SpatialMap<T> {

    fun add(x: Number, y: Number, item: T): Boolean

    fun remove(x: Number, y: Number, item: T): Boolean

    operator fun contains(point: Vector2): Boolean

    fun queryRange(x: Number, y: Number, width: Number, height: Number): List<T>

    fun clear()

    val size: Int

    fun isEmpty() = size == 0

    fun isNotEmpty() = !isEmpty()
}