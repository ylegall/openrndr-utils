package org.ygl.openrndr.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class QuadTreeTest {

    @Test
    fun `test quadtree add`() {
        val tree = QuadTree<Int>(100, 100)
        assertThat(tree.isEmpty()).isTrue()

        tree.add(25, 25, 1)
        assertThat(tree.isNotEmpty()).isTrue()
        assertThat(tree.size).isEqualTo(1)

        assertThat(tree.add(75, 25, 2)).isTrue()
        assertThat(tree.size).isEqualTo(2)

        assertThat(tree.add(175, -25, 3)).isFalse()
        assertThat(tree.size).isEqualTo(2)
    }

    @Test
    fun `test quadtree query range`() {
        val tree = QuadTree<Int>(100, 100)
        tree.add(25, 25, 1)
        tree.add(75, 25, 2)
        assertThat(tree.queryRange(0, 0, 100, 50)).isEqualTo(listOf(1, 2))
        assertThat(tree.queryRange(0, 0, 50, 100)).isEqualTo(listOf(1))
        assertThat(tree.queryRange(50, 0, 50, 100)).isEqualTo(listOf(2))
        assertThat(tree.queryRange(50, 50, 100, 50)).isEqualTo(emptyList<Int>())
    }

    @Test
    fun `test quadtree remove`() {
        val tree = QuadTree<Int>(100, 100)
        tree.add(25, 25, 1)
        tree.add(75, 25, 2)
        assertThat(tree.queryRange(0, 0, 100, 50)).isEqualTo(listOf(1, 2))
        assertThat(tree.remove(25, 25, 3)).isFalse()
        assertThat(tree.queryRange(0, 0, 100, 50)).isEqualTo(listOf(1, 2))
        assertThat(tree.remove(25, 25, 1)).isTrue()
        assertThat(tree.queryRange(0, 0, 100, 50)).isEqualTo(listOf(2))
        assertThat(tree.remove(75, 25, 2)).isTrue()
        assertThat(tree.queryRange(0, 0, 100, 50)).isEqualTo(emptyList<Int>())
        assertThat(tree.isEmpty()).isTrue()
    }

    @Test
    fun `test quad tree clear`() {
        val tree = QuadTree<Int>(100, 100)
        tree.add(25, 25, 1)
        tree.add(25, 26, 2)
        tree.add(26, 26, 3)
        tree.add(27, 25, 4)
        tree.add(26, 27, 5)
        assertThat(tree.size).isEqualTo(5)
        tree.clear()
        assertThat(tree.size).isEqualTo(0)
        assertThat(tree.isEmpty()).isTrue()
    }
}
