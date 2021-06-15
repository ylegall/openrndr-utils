package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.abs
import kotlin.math.sqrt

// https://lsandig.org/blog/2014/08/apollon-python/en/
// https://github.com/lsandig/apollon

fun perpindicularIntersection(v1: Vector2, v2: Vector2, v3: Vector2): Vector2 {
    val dx = v2.x - v1.x
    val dy = v2.y - v1.y
    val k = (dx * (v3.x - v1.x) + dy * (v3.y - v1.y)) / (dx * dx + dy * dy)
    val x = v1.x + k * dx
    val y = v1.y + k * dy
    return Vector2(x, y)
}

fun triangleIncenter(v1: Vector2, v2: Vector2, v3: Vector2): Vector2 {
    val l12 = v1.distanceTo(v2)
    val l23 = v2.distanceTo(v3)
    val l31 = v3.distanceTo(v1)
    val totalLength = l12 + l23 + l31
    val x = (l23 * v1.x + l31 * v2.x + l12 * v3.x) / totalLength
    val y = (l23 * v1.y + l31 * v2.y + l12 * v3.y) / totalLength
    return Vector2(x, y)
}

private fun outerTangentCircle(
    circles: List<Circle>,
    incenter: Vector2
): Circle {
    val c1 = circles[0]
    val c2 = circles[1]
    val c3 = circles[2]
    val k1 = 1.0 / c1.radius
    val k2 = 1.0 / c2.radius
    val k3 = 1.0 / c3.radius
    val cc1 = Complex(c1.center) * k1
    val cc2 = Complex(c2.center) * k2
    val cc3 = Complex(c3.center) * k3
    val k4 = -2.0 * sqrt(k1 * k2 + k2 * k3 + k1 * k3) + k1 + k2 + k3
    val r4 = 1.0 / k4
    val dis = cc1 * cc2 + cc2 * cc3 + cc1 * cc3

    // TODO: everytime there is a square root, there are 2 solutions.
    // TODO: figure out how to pick the best one so that the animation does not "jump"
    val p4 = ((dis.sqrt() * 2.0) + cc1 + cc2 + cc3) * r4
    val p5 = ((dis.sqrt() * -2.0) + cc1 + cc2 + cc3) * r4
    val p = if (p4.toVector2().distanceTo(incenter) < p5.toVector2().distanceTo(incenter)) p4 else p5
    return Circle(p.toVector2(), abs(r4))
}

private fun secondSolution(fixed: Circle, c1: Circle, c2: Circle, c3: Circle): Circle {
    val curf = 1.0 / fixed.radius
    val cur1 = 1.0 / c1.radius
    val cur2 = 1.0 / c2.radius
    val cur3 = 1.0 / c3.radius

    val newCurvature = 2 * (cur1 + cur2 + cur3) - curf
    val pos = ((c1.center * cur1 + c2.center * cur2 + c3.center * cur3) * 2.0 - fixed.center * curf) / newCurvature
    return Circle(pos, 1.0 / newCurvature)
}

private fun generateCirclesRecursive(
    circles: List<Circle>,
    level: Int,
    minRadius: Double?,
    maxLevel: Int?,
    generated: MutableList<Circle>
) {
    if (maxLevel != null && level >= maxLevel) {
        return
    }
    val c1 = circles[0]
    val c2 = circles[1]
    val c3 = circles[2]
    val c4 = circles[3]

    val newCircle2 = secondSolution(c2, c1, c3, c4)
    generated.add(newCircle2)
    val newCircle3 = secondSolution(c3, c1, c2, c4)
    generated.add(newCircle3)
    val newCircle4 = secondSolution(c4, c1, c2, c3)
    generated.add(newCircle4)

    if (minRadius != null && newCircle2.radius > minRadius)
    generateCirclesRecursive(listOf(newCircle2, c1, c3, c4), level + 1, minRadius, maxLevel, generated)
    if (minRadius != null && newCircle3.radius > minRadius)
    generateCirclesRecursive(listOf(newCircle3, c1, c2, c4), level + 1, minRadius, maxLevel, generated)
    if (minRadius != null && newCircle4.radius > minRadius)
    generateCirclesRecursive(listOf(newCircle4, c1, c2, c3), level + 1, minRadius, maxLevel, generated)
}

private fun generateCircles(
    outerCircle: Circle,
    initialCircles: List<Circle>,
    minRadius: Double?,
    maxLevel: Int?
): List<Circle> {
    val outer = Circle(outerCircle.center, -outerCircle.radius)
    val c0 = secondSolution(outer, initialCircles[0], initialCircles[1], initialCircles[2])
    val c1 = initialCircles[0]
    val c2 = initialCircles[1]
    val c3 = initialCircles[2]
    val generatedCircles = mutableListOf(c0)
    generatedCircles.addAll(initialCircles)
    generateCirclesRecursive(listOf(c0, c1, c2, c3), 0, minRadius, maxLevel, generatedCircles)
    generateCirclesRecursive(listOf(outer, c1, c2, c3), 0, minRadius, maxLevel, generatedCircles)
    return generatedCircles
}

fun normalizeCircles(
    outerRadius: Double,
    outerCircle: Circle,
    circles: List<Circle>
): List<Circle> {
    val positionOffset = outerCircle.center
    val scaleOffset = outerRadius / outerCircle.radius
    return circles.map {
        Circle((it.center - positionOffset) * scaleOffset, it.radius * scaleOffset)
    }
}

fun apollonianGasket(
    startingPoints: List<Vector2>,
    outerRadius: Double,
    maxDepth: Int? = 4,
    minRadius: Double? = null
): List<Circle> {
    require(startingPoints.size == 3)
    require(maxDepth != null || minRadius != null)
    val incenter = triangleIncenter(startingPoints[0], startingPoints[1], startingPoints[2])
    val p01 = perpindicularIntersection(startingPoints[0], startingPoints[1], incenter)
    val p12 = perpindicularIntersection(startingPoints[1], startingPoints[2], incenter)
    val initialCircles = listOf(
        Circle(startingPoints[0], p01.distanceTo(startingPoints[0])),
        Circle(startingPoints[1], p12.distanceTo(startingPoints[1])),
        Circle(startingPoints[2], p12.distanceTo(startingPoints[2])),
    )
    val outerCircle = outerTangentCircle(initialCircles, incenter)
    val allCircles = generateCircles(outerCircle, initialCircles, minRadius, maxDepth)
    return normalizeCircles(outerRadius, outerCircle, allCircles)
}