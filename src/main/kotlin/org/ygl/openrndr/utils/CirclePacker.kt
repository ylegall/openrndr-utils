package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sqrt


class CirclePacker(
        val params: CirclePackParams
) {

    private val circles = mutableSetOf<Circle>()

    fun getCircles(): Set<Circle> = circles

    fun clear() {
        circles.clear()
    }

    fun save(fileName: String) {
        val file = File(fileName)
        FileWriter(file).buffered().use {
            circles.forEach { circle ->
                it.write("${circle.center.x},${circle.center.y},${circle.radius}")
                it.newLine()
            }
        }
    }

    fun load(fileName: String) {
        circles.clear()
        val file = File(fileName)
        FileReader(file).buffered().use {
            it.lineSequence().forEach { line ->
                val tokens = line.split(",")
                val pos = Vector2(tokens[0].toDouble(), tokens[1].toDouble())
                val radius = tokens[2].toDouble()
                circles.add(Circle(pos, radius))
            }
        }
    }

    fun loadOrGenerate(fileName: String): Set<Circle> {
        if (File(fileName).exists()) {
            load(fileName)
        } else {
            packCircles()
            save(fileName)
        }
        return circles
    }

    fun packCircles() {
        if (params.circleLimit == null && params.targetFillRatio == null) {
            throw IllegalArgumentException("limit and targetFillRatio can't both be null")
        }
        var filledArea = 0.0
        var limitReached = false

        while (!limitReached) {
            val newCircles = if (params.wrap) {
                findCirclesWithWrapping()
            } else {
                findCirclesNoWrapping()
            }
            if (newCircles.isNotEmpty()) {

                println(circles.size)
                if (params.circleLimit != null && (circles.size + 1) >= params.circleLimit) {
                    limitReached = true
                }

                for (c in newCircles) {
                    params.spatialMap.add(c.center.x, c.center.y, c)
                    circles.add(c)
                }

                filledArea += newCircles.take(1).map { it.radius * it.radius * PI }.sum()
                val fillRatio = filledArea / params.bounds.area
                println(fillRatio)
                if (params.targetFillRatio != null && fillRatio >= params.targetFillRatio) {
                    limitReached = true
                }
            }
        }
        println("circles: ${circles.size}")
        println("fill ratio: ${filledArea/params.bounds.area}")
    }

    private fun findCirclesNoWrapping(): List<Circle> {
        val (pos, neighbors) = findPointAndNeighbors()
        val distFromBoundary = pos.minDistanceFromBoundary()
        val minDistFromNeighbors = pos.minDistanceFromNeighbors(neighbors)
        val minDist = min(distFromBoundary, minDistFromNeighbors)
        if (minDist < params.minRadius) {
            return emptyList()
        }
        val radius = minDist.coerceAtMost(params.maxRadius)
        //val newCircle = Circle(pos, radius)
        //params.spatialMap.add(pos.x, pos.y, newCircle)
        //circles.add(newCircle)
        return listOf(Circle(pos, radius))
    }

    private fun findCirclesWithWrapping(): List<Circle> {
        val (pos, neighbors) = findPointAndNeighbors()
        val distFromBoundary = pos.minDistanceFromBoundary()

        val allPoints = mutableListOf(pos)
        var minDist = pos.minDistanceFromNeighbors(neighbors)
        if (minDist < params.minRadius) {
            return emptyList()
        }

        if (distFromBoundary < params.maxRadius) {
            if (pos.x < params.bounds.x + params.maxRadius) {
                val left = pos + Vector2(params.bounds.width, 0.0)
                val leftDist = left.minDistFromNeighbors() ?: return emptyList()
                allPoints.add(left)
                minDist = min(minDist, leftDist)
            } else if (pos.x + params.maxRadius > params.bounds.x + params.bounds.width) {
                val right = pos - Vector2(params.bounds.width, 0.0)
                val rightDist = right.minDistFromNeighbors() ?: return emptyList()
                allPoints.add(right)
                minDist = min(minDist, rightDist)
            }
            if (pos.y < params.bounds.y + params.maxRadius) {
                val down = pos + Vector2(0.0, params.bounds.height)
                val downDist = down.minDistFromNeighbors() ?: return emptyList()
                allPoints.add(down)
                minDist = min(minDist, downDist)
            } else if (pos.y + params.maxRadius > params.bounds.y + params.bounds.height) {
                val up = pos - Vector2(0.0, params.bounds.height)
                val upDist = up.minDistFromNeighbors() ?: return emptyList()
                allPoints.add(up)
                minDist = min(minDist, upDist)
            }
            val radius = minDist
            if (radius < params.minRadius) return emptyList()
        }

        return allPoints.map { Circle(it, minDist) }
    }

    private fun Vector2.minDistFromNeighbors(): Double? {
        val neighbors = this.neighbors() ?: return null
        return this.minDistanceFromNeighbors(neighbors).takeIf { it >= params.minRadius }
    }

    private fun findPointAndNeighbors(): Pair<Vector2, List<Circle>> {
        var pos: Vector2
        var neighbors: List<Circle>? = null
        do {
            pos = params.bounds.randomPoint()
            if (!params.maskFunction(pos)) continue
            neighbors = pos.neighbors()
        } while (neighbors == null)
        return pos to neighbors
    }

    private fun Vector2.distanceTo(circle: Circle) = circle.center.distanceTo(this) - circle.radius - params.padding

    private fun Vector2.minDistanceFromNeighbors(circles: List<Circle>) = circles.map {
        this.distanceTo(it)
    }.minOrNull() ?: params.maxRadius

    private fun Vector2.minDistanceFromBoundary() = min(
            //min(x - params.bounds.x, params.bounds.x + params.bounds.width - x + params.bounds.x),
            //min(y - params.bounds.y, params.bounds.y + params.bounds.height - y + params.bounds.y)
        min(x - params.bounds.x, params.bounds.x + params.bounds.width - x),
        min(y - params.bounds.y, params.bounds.y + params.bounds.height - y)
    )

    // null means that we are overlapping
    private fun Vector2.neighbors(): List<Circle>? {
        val maxDist = 2 * params.maxRadius + params.padding
        //val nearbyCircles = params.spatialMap.queryRange(this.x - maxDist, this.y - maxDist, 2 * maxDist, 2 * maxDist).filter {
        //    this.distanceTo(it) <= maxDist
        //}
        val searchArea = Rectangle.fromCenter(this, 2 * sqrt(2.0) * params.maxRadius)
        val nearbyCircles = params.spatialMap.queryRange(
            searchArea.corner.x,
            searchArea.corner.y,
            searchArea.corner.x + searchArea.width,
            searchArea.corner.y + searchArea.height
        ).filter {
            this.distanceTo(it) <= maxDist
        }

        return if (nearbyCircles.any { it.contains(this) }) {
            null
        } else {
            nearbyCircles
        }
    }

    //private fun Vector2.overlapsAny(): Boolean {
    //    val queryBounds = Rectangle.fromCenter(this, 2 * params.maxRadius, 2 * params.maxRadius)
    //    val nearbyCircles = params.spatialMap.queryRange(queryBounds.x, queryBounds.y, queryBounds.width, queryBounds.height)
    //    return nearbyCircles.any { it.contains(this) }
    //}
}

fun circlePackingFromPoints(
    points: List<Vector2>,
    maxRadius: Double,
    minRadius: Double = 0.0,
): List<Circle> {
    if (points.isEmpty()) {
        return emptyList()
    }
    val kd = KDTree2.fromPoints(points.indices.toList()) { points[it] }
    val radii = MutableList(points.size) { minRadius }
    for (i in points.indices) {
        val bounds = Rectangle.fromCenter(points[i], 4 * maxRadius)
        val neighbors = kd.queryRange(bounds)
        val minDist = neighbors.asSequence().filter {
            it != i
        }.map {
            points[i].distanceTo(points[it]) - radii[it]
        }.minOrNull() ?: maxRadius * 0.7
        radii[i] = minDist
    }
    return points.zip(radii).map { Circle(it.first, it.second) }
}
