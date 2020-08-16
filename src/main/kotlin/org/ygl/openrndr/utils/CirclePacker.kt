package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.math.PI
import kotlin.random.Random

enum class RadiusSelectionType {
    RANDOM,
    DECAY
}

class CirclePackParams(
        val bounds: Rectangle,
        val minRadius: Double = 10.0,
        val maxRadius: Double = 100.0,
        val spatialMap: SpatialMap<Circle> = TileGrid(bounds.offsetEdges(maxRadius, maxRadius),
                (minRadius + maxRadius)/2.0,
                (minRadius + maxRadius)/2.0
        ),
        val wrap: Boolean = false,
        val radiusSelectionType: RadiusSelectionType = RadiusSelectionType.RANDOM
)

class CirclePacker(
        val params: CirclePackParams
) {
    private val circles = mutableSetOf<Circle>()
    private var size = 0

    fun getCircles(): Set<Circle> = circles

    fun packToLimit(limit: Int) {
        packCircles(limit, null)
    }

    fun packToTargetRatio(targetFillRatio: Double) {
        packCircles(null, targetFillRatio)
    }

    fun loadOrGenerate(fileName: String, limit: Int?, targetFillRatio: Double?) {
        when {
            File(fileName).exists() -> load(fileName)
            limit != null -> packToLimit(limit)
            targetFillRatio != null -> packToTargetRatio(targetFillRatio)
            else -> throw Exception("limit and targetFillRatio both can't be null")
        }
    }

    //private fun Circle.overlaps(pos: Vector2, otherRadius: Double): Boolean {
    private fun Circle.overlaps(otherCircle: Circle): Boolean {
        val distance = center.distanceTo(otherCircle.center)
        return distance < radius + otherCircle.radius
    }

    private fun Circle.overlaps(circles: List<Circle>) = circles.any { it.overlaps(this) }

    private fun Circle.getNeighbors(): List<Circle> {
        val maxDist = radius + params.maxRadius
        return params.spatialMap.queryRange(center.x - maxDist, center.y - maxDist, 2 * maxDist, 2 * maxDist)
    }

    private fun addCircle(radius: Double): List<Circle> {
        var attempts = 0
        while (attempts < 5) {
            val pos = params.bounds.randomPoint()
            val newCircles = expand(pos, radius)
            val neighbors = newCircles.flatMap { it.getNeighbors() }
            if (newCircles.none { it.overlaps(neighbors) }) {
                newCircles.forEach { params.spatialMap.add(it.center.x, it.center.y, it) }
                circles.addAll(newCircles)
                return newCircles
            }
            attempts++
        }
        return emptyList()
    }

    // TODO: dynamically lower radius
    private fun packCircles(limit: Int? = null, targetFillRatio: Double? = null) {
        var filledArea = 0.0
        var limitReached = false

        var currentSize = if (params.radiusSelectionType == RadiusSelectionType.DECAY) {
            params.maxRadius
        } else {
            Random.nextDouble(params.minRadius, params.maxRadius)
        }

        while (!limitReached) {
            val radius = currentSize
            val newCircles = addCircle(radius)
            if (newCircles.isNotEmpty()) {
                size++
                println(size)
                if (limit != null && size >= limit) {
                    limitReached = true
                }
                filledArea += newCircles.map { it.radius * it.radius * PI }.sum()
                val fillRatio = filledArea / params.bounds.area
                println(fillRatio)
                if (targetFillRatio != null && fillRatio >= targetFillRatio) {
                    limitReached = true
                }
            }

            currentSize = if (params.radiusSelectionType == RadiusSelectionType.DECAY) {
                if (newCircles.isEmpty() && currentSize * 0.99 > params.minRadius) {
                    currentSize * 0.99
                } else {
                    currentSize
                }
            } else {
                Random.nextDouble(params.minRadius, params.maxRadius)
            }
        }
        println("circles: $size")
        println("fill ratio: ${filledArea/params.bounds.area}")
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

    private fun expand(pos: Vector2, radius: Double): List<Circle> {
        return if (params.wrap) {
            val expandedCircles = mutableListOf(Circle(pos, radius))
            if (pos.x - radius < 0) {
                expandedCircles.add(Circle(pos + vector2(params.bounds.width, 0.0), radius))
            } else if (pos.x + radius > params.bounds.width) {
                expandedCircles.add(Circle(pos - vector2(params.bounds.width, 0.0), radius))
            }
            if (pos.y - radius < 0) {
                expandedCircles.add(Circle(pos + vector2(0.0, params.bounds.height), radius))
            } else if (pos.y + radius > params.bounds.height) {
                expandedCircles.add(Circle(pos - vector2(0.0, params.bounds.height), radius))
            }
            expandedCircles
        } else {
            listOf(Circle(pos, radius))
        }
    }

    fun clear() {
        circles.clear()
    }
}
