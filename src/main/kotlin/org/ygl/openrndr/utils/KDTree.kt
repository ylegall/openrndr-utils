package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.round

class KDTree<T>: SpatialMap<T> {

    enum class Axis { X, Y }

    class Entry<T>(val key: Vector2, val values: List<T>)

    sealed class TreeNode {
        object Empty: TreeNode()

        class Leaf<T>(
                val entries: List<Entry<T>>
        ): TreeNode()

        class Branch(
                val left: TreeNode,
                val right: TreeNode,
                val axis: Axis,
                val axisValue: Double
        ): TreeNode()
    }

    private var root: TreeNode = TreeNode.Empty

    override var size: Int = 0; private set

    override fun add(x: Number, y: Number, item: T): Boolean {
        TODO("Not implemented")
    }

    override fun remove(x: Number, y: Number, item: T): Boolean {
        TODO("Not implemented")
    }

    @Suppress("UNCHECKED_CAST")
    fun find(point: Vector2): List<T> {
        val point = point.truncate()
        var node = root
        while (node is TreeNode.Branch) {
            node = when(node.axis) {
                Axis.X -> if (point.x < node.axisValue) node.left else node.right
                Axis.Y -> if (point.y < node.axisValue) node.left else node.right
            }
        }

        return when (node) {
            TreeNode.Empty      -> emptyList()
            is TreeNode.Leaf<*> -> node.entries.find { point == it.key }?.values as List<T>
            else                -> throw IllegalStateException()
        }
    }

    fun nearestNeighbor(point: Vector2): List<T> {
        return nearestNeighbor(point, root)?.items ?: emptyList()
    }

    class NeighborResult<T>(val items: List<T>, val distance: Double)

    private fun nearestNeighbor(point: Vector2, node: TreeNode): NeighborResult<T>? {
        return when(node) {
            TreeNode.Empty -> null
            is TreeNode.Leaf<*> -> {
                node.entries.map {
                    it to it.key.distanceTo(point)
                }.minBy {
                    it.second
                }?.let {
                    NeighborResult(it.first.values as List<T>, it.second)
                }
            }
            is TreeNode.Branch -> {
                val near: TreeNode
                val far: TreeNode
                if (node.axis == Axis.X) {
                    if (point.x < node.axisValue) {
                        near = node.left
                        far = node.right
                    } else {
                        near = node.right
                        far = node.left
                    }
                } else {
                    if (point.y < node.axisValue) {
                        near = node.left
                        far = node.right
                    } else {
                        near = node.left
                        far = node.right
                    }
                }
                val results = nearestNeighbor(point, near)
                val minFarDist = when (node.axis) {
                    Axis.X -> abs(node.axisValue - point.x)
                    Axis.Y -> abs(node.axisValue - point.y)
                }

                return when {
                    results == null -> {
                        nearestNeighbor(point, far)
                    }
                    results.distance >= minFarDist -> {
                        val results2 = nearestNeighbor(point, far)
                        if (results2 != null && results.distance < results2.distance) results else results2
                    }
                    else -> {
                        results
                    }
                }
            }
        }
    }

    override fun contains(point: Vector2): Boolean {
        return find(point).isNotEmpty()
    }

    override fun queryRange(x: Number, y: Number, width: Number, height: Number): List<T> {
        return queryRange(Rectangle(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), root)
    }

    fun queryRange(bounds: Rectangle): List<T> {
        return queryRange(bounds, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun queryRange(bounds: Rectangle, node: TreeNode): List<T> {
        return when (node) {
            TreeNode.Empty      -> emptyList()
            is TreeNode.Leaf<*> -> {
                node.entries.filter { it.key in bounds }.flatMap { it.values } as List<T>
            }
            is TreeNode.Branch  -> {
                val leftItems = if (
                        (node.axis == Axis.X && bounds.x < node.axisValue) ||
                        (node.axis == Axis.Y && bounds.y < node.axisValue)
                ) {
                    queryRange(bounds, node.left)
                } else {
                    emptyList()
                }
                val rightItems = if (
                        (node.axis == Axis.X && (bounds.x + bounds.width) >= node.axisValue) ||
                        (node.axis == Axis.Y && (bounds.y + bounds.height) >= node.axisValue)
                ) {
                    queryRange(bounds, node.right)
                } else {
                    emptyList()
                }
                leftItems + rightItems
            }
        }
    }

    override fun clear() {
        size = 0
        root = TreeNode.Empty
    }

    companion object {

        private fun Vector2.truncate() = Vector2(x.truncate(), y.truncate())

        fun <T> fromPoints(items: List<T>, pointMapper: (T) -> Vector2): KDTree<T> {
            val xs = items.sortedBy { pointMapper(it).x }.groupBy { pointMapper(it).truncate() }
            val ys = items.sortedBy { pointMapper(it).y }.groupBy { pointMapper(it).truncate() }

            val tree = KDTree<T>()
            tree.root = fromPoints(xs, ys)
            tree.size = items.size
            return tree
        }

        //private fun median(items: List<Double>) = items[items.size / 2]
        private fun median(items: List<Double>) = (items.first() + items[items.size / 2] + items.last())/3

        private fun <T> fromPoints(xs: Map<Vector2, List<T>>, ys: Map<Vector2, List<T>>): TreeNode {
            //println(xs)
            //println(ys)
            //println()
            return if (xs.isEmpty()) {
                TreeNode.Empty
            } else if (xs.size <= 8) {
                TreeNode.Leaf(xs.entries.map { Entry(it.key, it.value) })
            } else {
                val dx = abs(xs.entries.first().key.x - xs.entries.last().key.x)
                val dy = abs(ys.entries.first().key.y - ys.entries.last().key.y)

                if (dx > dy) {
                    val axis = Axis.X
                    val midValue = median(xs.entries.map { it.key.x } )
                    val (leftXs, rightXs) = xs.entries.partition { (point, _) -> point.x < midValue }.let { (left, right) ->
                        left.associate { it.toPair() } to right.associate { it.toPair() }
                    }
                    val (leftYs, rightYs) = ys.entries.partition { it.key in leftXs }.let { (left, right) ->
                        left.associate { it.toPair() } to right.associate { it.toPair() }
                    }
                    val left = fromPoints(leftXs, leftYs)
                    val right = fromPoints(rightXs, rightYs)
                    TreeNode.Branch(left, right, axis, midValue)
                } else {
                    val axis = Axis.Y
                    val midValue = median(ys.entries.map { it.key.y } )
                    val (leftYs, rightYs) = ys.entries.partition { (point, _) -> point.y < midValue }.let { (left, right) ->
                        left.associate { it.toPair() } to right.associate { it.toPair() }
                    }
                    val (leftXs, rightXs) = xs.entries.partition { it.key in leftYs }.let { (left, right) ->
                        left.associate { it.toPair() } to right.associate { it.toPair() }
                    }
                    val left = fromPoints(leftXs, leftYs)
                    val right = fromPoints(rightXs, rightYs)
                    TreeNode.Branch(left, right, axis, midValue)
                }
            }
        }

        private fun Double.truncate(): Double {
            val multiplier = 1000.0
            return round(this * multiplier) / multiplier
        }
    }

}
