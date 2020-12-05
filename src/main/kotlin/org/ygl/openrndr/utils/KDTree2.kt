package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.round


class KDTree2<T>(

): SpatialMap<T> {

    enum class Axis { X, Y }

    sealed class TreeNode {
        object Empty: TreeNode()

        class Leaf<T>(
                val point: Vector2,
                //val value: T
                val values: List<T>
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

    //private fun split(x: Double, y: Double, item: T, node: TreeNode.Leaf<*>): TreeNode.Branch {
    //    val newNode = TreeNode.Leaf(Vector2(x, y), item)
    //
    //    val dx = abs(x - node.point.x)
    //    val dy = abs(y - node.point.y)
    //    val axis = if (dx > dy) Axis.X else Axis.Y
    //    val left: TreeNode
    //    val right: TreeNode
    //    val splitVaule: Double
    //
    //    if (axis == Axis.X) {
    //        splitVaule = (x + node.point.x) / 2
    //        if (x < node.point.x) {
    //            left = newNode
    //            right = node
    //        } else {
    //            right = newNode
    //            left = node
    //        }
    //    } else {
    //        splitVaule = (y + node.point.y) / 2
    //        if (y < node.point.y) {
    //            left = newNode
    //            right = node
    //        } else {
    //            right = newNode
    //            left = node
    //        }
    //    }
    //
    //    return TreeNode.Branch(left, right, axis, splitVaule)
    //}

    override fun add(x: Number, y: Number, item: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun remove(x: Number, y: Number, item: T): Boolean {
        TODO("Not yet implemented")
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
            is TreeNode.Leaf<*> -> if (node.point == point) node.values as List<T> else emptyList()
            else                -> throw IllegalStateException()
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
            //is TreeNode.Leaf<*> -> if (node.point in bounds) listOf(node.value as T) else emptyList()
            is TreeNode.Leaf<*> -> if (node.point in bounds) node.values as List<T> else emptyList()
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

        fun <T> fromPoints(items: List<T>, pointMapper: (T) -> Vector2): KDTree2<T> {
            val xs = items.sortedBy { pointMapper(it).x }.groupBy { pointMapper(it).truncate() }
            val ys = items.sortedBy { pointMapper(it).y }.groupBy { pointMapper(it).truncate() }

            val tree = KDTree2<T>()
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
            } else if (xs.size == 1) {
                val entry = xs.entries.first()
                TreeNode.Leaf(entry.key, entry.value)
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
