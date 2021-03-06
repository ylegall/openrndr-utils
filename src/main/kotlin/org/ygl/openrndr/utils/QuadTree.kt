package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.intersects


class QuadTree<T>(
        val bounds: Rectangle,
        val maxRegionCapacity: Int = 4
): SpatialMap<T> {

    constructor(width: Number, height: Number): this(
            rect(0, 0, width, height)
    )

    data class QuadTreeEntry<V>(
            val position: Vector2,
            val value: V
    )

    private var children: List<QuadTree<T>> = emptyList()
    private var entries: MutableList<QuadTreeEntry<T>> = mutableListOf()
    override var size = 0; private set

    override fun add(x: Number, y: Number, item: T) = add(vector2(x, y), item)

    fun add(position: Vector2, item: T): Boolean {
        return when {
            position !in bounds -> false
            children.isNotEmpty() -> addToChildren(QuadTreeEntry(position, item))
            entries.size < maxRegionCapacity -> entries.add(QuadTreeEntry(position, item))
            else -> {
                subdivide()
                addToChildren(QuadTreeEntry(position, item))
            }
        }.also { addSucceeded ->
            if (addSucceeded) {
                size++
            }
        }
    }

    override fun queryRange(x: Number, y: Number, width: Number, height: Number) = queryRange(rect(x, y, width, height))

    fun queryRange(rectangle: Rectangle): List<T> {
        return when {
            !intersects(bounds, rectangle) -> emptyList()
            children.isEmpty() -> entries.filter { it.position in rectangle }.map { it.value }
            else -> children.map { it.queryRange(rectangle) }.flatten()
        }
    }

    override fun contains(point: Vector2): Boolean {
        return when {
            point !in bounds -> false
            children.isEmpty() -> entries.any { it.position == point }
            else -> children.any { it.contains(point) }
        }
    }

    override fun remove(x: Number, y: Number, item: T) = remove(vector2(x, y), item)

    fun remove(point: Vector2, item: T): Boolean {
        return removeInternal(QuadTreeEntry(point, item))
    }

    override fun clear() {
        children = emptyList()
        entries.clear()
        size = 0
    }

    private fun removeInternal(entry: QuadTreeEntry<T>): Boolean {
        return when {
            entry.position !in bounds -> false
            children.isEmpty() -> entries.remove(entry)
            else -> {
                val removed = children.find { entry.position in it.bounds }?.removeInternal(entry) ?: false
                if (removed) {
                    if (children.sumBy { it.size } == 0) {
                        children = emptyList()
                    }
                }
                removed
            }
        }.also { removed ->
            if (removed) {
                size--
            }
        }
    }

    private fun subdivide() {
        val childWith = bounds.width / 2
        val childHeight = bounds.height / 2
        children = listOf(
                QuadTree(rect(bounds.x, bounds.y, childWith, childHeight)),
                QuadTree(rect(bounds.x + childWith, bounds.y, childWith, childHeight)),
                QuadTree(rect(bounds.x, bounds.y + childHeight, childWith, childHeight)),
                QuadTree(rect(bounds.x + childWith, bounds.y + childHeight, childWith, childHeight))
        )
        entries.forEach { addToChildren(it) }
        entries.clear()
    }

    private fun addToChildren(entry: QuadTreeEntry<T>): Boolean {
        return children.find {
            entry.position in it.bounds
        }?.add(entry.position, entry.value) ?: false
    }
}