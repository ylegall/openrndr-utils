package org.ygl.openrndr.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

internal class KDTreeTest
{

    @Test
    fun `test duplicates`() {
        val items = listOf(
                Pair("a", Vector2.ZERO),
                Pair("b", Vector2.ZERO),
                Pair("c", Vector2(-2.0, 0.0)),
                Pair("g", Vector2(2.0, 0.0))
        )
        val tree = KDTree.fromPoints(items) { it.second }
        assertThat(tree.size).isEqualTo(items.size)
        assertThat(tree.find(Vector2.ZERO)).hasSameElementsAs(items.take(2))
    }

    @Test
    fun `test partition`() {
        val items = listOf(
                Pair("a", Vector2(x=-48.5574434108823, y=79.9)),
                Pair("b", Vector2(x=-48.55744341088231, y=79.9))
        )
        val tree = KDTree.fromPoints(items) { it.second }
        assertThat(tree.size).isEqualTo(items.size)
        assertThat(tree.find(Vector2(x=-48.55744341088231, y=79.9))).hasSameElementsAs(items.takeLast(2))
    }

    @Test
    fun `test find`() {
        val items = listOf(
                Pair("a", Vector2.ZERO),
                Pair("b", Vector2(0.0, 1.0)),
                Pair("c", Vector2(1.0, 0.0)),
                Pair("d", Vector2(1.0, 1.0)),
                Pair("e", Vector2(0.0, -1.0)),
                Pair("f", Vector2(-1.0, 0.0)),
                Pair("g", Vector2(-1.0, -1.0))
        )
        val tree = KDTree.fromPoints(items) { it.second }
        assertThat(tree.size).isEqualTo(items.size)

        for (item in items) {
            assertThat(tree.find(item.second).first()).isEqualTo(item)
        }

        assertThat(tree.find(Vector2(2.0, 0.0))).isEmpty()
        assertThat(tree.find(Vector2(0.0, 0.5))).isEmpty()
    }

    @Test
    fun `test query range`() {
        val items = listOf(
                Pair("a1", Vector2(-2.0, 2.0)),
                Pair("a2", Vector2(2.0, 2.0)),
                Pair("a3", Vector2(2.0, -2.0)),
                Pair("a4", Vector2(-2.0, -2.0)),
                Pair("b1", Vector2(-1.0, 1.0)),
                Pair("b2", Vector2(1.0, 1.0)),
                Pair("b3", Vector2(1.0, -1.0)),
                Pair("b4", Vector2(-1.0, -1.0))
        )
        val tree = KDTree.fromPoints(items) { it.second }
        val hits = tree.queryRange(Rectangle(-1.5, -1.5, 3.0, 3.0))
        val foundNames = hits.map { it.first }.toSet()
        assertThat(foundNames).containsAll(setOf("b1", "b2", "b3", "b4"))
        assertThat(foundNames).doesNotContainAnyElementsOf(setOf("a1", "a2", "a3", "a4"))
    }
}

