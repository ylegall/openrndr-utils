package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.floor


class TileGrid<T>(
        val bounds: Rectangle,
        val tileWidth: Double,
        val tileHeight: Double
): SpatialMap<T> {

    constructor(
            x: Number,
            y: Number,
            width: Number,
            height: Number,
            tileWidth: Number,
            tileHeight: Number
    ): this(
            rect(x, y, width, height), tileWidth.toDouble(), tileHeight.toDouble()
    )

    private val buckets = HashMap<Vector2, HashSet<T>>()
    override var size = 0; private set

    override fun add(x: Number, y: Number, item: T) = add(vector2(x, y), item)

    fun add(point: Vector2, item: T): Boolean {
        return if (point !in bounds) {
            false
        } else {
            val key = getTile(point)
            buckets.getOrPut(key) { HashSet() }.add(item)
        }.also { addSuccess ->
            if (addSuccess) {
                size++
            }
        }
    }

    override fun remove(x: Number, y: Number, item: T) = remove(vector2(x, y), item)

    fun remove(point: Vector2, item: T): Boolean {
        return if (point !in bounds) {
            false
        } else {
            val key = getTile(point)
            buckets[key]?.remove(item) ?: false
        }.also { addSuccess ->
            if (addSuccess) {
                size--
            }
        }
    }

    // TODO: maybe store Entry for exact match
    override fun contains(point: Vector2): Boolean {
        return when (point) {
            !in bounds -> false
            else -> buckets[getTile(point)]?.isNotEmpty() ?: false
        }
    }

    override fun queryRange(x: Number, y: Number, width: Number, height: Number) = queryRange(rect(x, y, width, height))

    fun queryRange(rectangle: Rectangle): List<T> {
        val minY = floor(rectangle.y / tileHeight).toInt()
        val minX = floor(rectangle.x / tileWidth).toInt()
        val maxY = floor((rectangle.y + rectangle.height) / tileHeight).toInt()
        val maxX = floor((rectangle.x + rectangle.width) / tileWidth).toInt()

        return (minY .. maxY).flatMap { y ->
            (minX .. maxX).mapNotNull { x ->
                buckets[vector2(x, y)]
            }.flatten()
        }
    }

    override fun clear() {
        buckets.clear()
        size = 0
    }

    private fun getTile(point: Vector2) = Vector2(
            floor(point.x / tileWidth),
            floor(point.y / tileHeight)
    )
}
