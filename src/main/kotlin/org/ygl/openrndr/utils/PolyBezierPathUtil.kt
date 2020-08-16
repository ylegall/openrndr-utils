package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.ShapeContour

fun smoothCurve(points: List<Vector2>, closed: Boolean = false): ShapeContour {
    val builder = ContourBuilder(false)

    val adjustedPoints = if (closed) {
        points + points.first()
    } else {
        points
    }

    val (c1, c2) = getCurveControlPoints(adjustedPoints)

    builder.moveTo(adjustedPoints.first())
    for (i in 1 until adjustedPoints.size) {
        builder.curveTo(c1[i - 1], c2[i - 1], adjustedPoints[i])
    }

    if (closed) builder.close()
    return builder.result.first()
}

/**
 * https://www.codeproject.com/Articles/31859/Draw-a-Smooth-Curve-through-a-Set-of-2D-Points-wit
 */
private fun getCurveControlPoints(points: List<Vector2>): Pair<List<Vector2>, List<Vector2>> {
    check(points.size >= 2) { "At least two points required" }

    return if (points.size == 2) {
        getSingleSegmentControlPoints(points[0], points[1])
    } else {
        getPathControlPoints(points)
    }
}

private fun getPathControlPoints(points: List<Vector2>): Pair<List<Vector2>, List<Vector2>> {
    val n = points.size - 1

    val firstControlPoints = MutableList(n) { Vector2.ZERO }
    val secondControlPoints = MutableList(n) { Vector2.ZERO }

    val rhs = DoubleArray(n)

    // Set right hand side X values
    for (i in 1 until n - 1) {
        rhs[i] = 4 * points[i].x + 2 * points[i + 1].x
    }

    rhs[0] = points[0].x + 2 * points[1].x
    rhs[n - 1] = (8 * points[n - 1].x + points[n].x) / 2.0

    // Get first control points X-values
    val x = getFirstControlPoints(rhs)

    // Set right hand side Y values
    for (i in 1 until n - 1) {
        rhs[i] = 4 * points[i].y + 2 * points[i + 1].y
    }
    rhs[0] = points[0].y + 2 * points[1].y
    rhs[n - 1] = (8 * points[n - 1].y + points[n].y) / 2.0

    // Get first control points Y-values
    val y = getFirstControlPoints(rhs)

    for (i in 0 until n) {
        firstControlPoints[i] = Vector2(x[i], y[i])
        if (i < n - 1) {
            secondControlPoints[i] = Vector2(
                    2 * points[i + 1].x - x[i + 1],
                    2 * points[i + 1].y - y[i + 1]
            )
        } else {
            secondControlPoints[i] = Vector2(
                    (points[n].x + x[n - 1]) / 2,
                    (points[n].y + y[n - 1]) / 2
            )
        }
    }
    return firstControlPoints to secondControlPoints
}

private fun getSingleSegmentControlPoints(p1: Vector2, p2: Vector2): Pair<List<Vector2>, List<Vector2>> {
    // Special case: Bezier curve should be a straight line.
    val c1 = Vector2((2 * p1.x + p2.x) / 3, (2 * p1.y + p2.y) / 3)
    val c2 = Vector2(2 * c1.x - p1.x, 2 * c1.y - p1.y)
    return listOf(c1) to listOf(c2)
}

private fun getFirstControlPoints(rhs: DoubleArray): DoubleArray {
    val n: Int = rhs.size
    val x = DoubleArray(n) // Solution vector.
    val tmp = DoubleArray(n) // Temp workspace.
    var b = 2.0
    x[0] = rhs[0] / b
    for (i in 1 until n) {  // Decomposition and forward substitution.
        tmp[i] = 1 / b
        b = (if (i < n - 1) 4.0 else 3.5) - tmp[i]
        x[i] = (rhs[i] - x[i - 1]) / b
    }
    for (i in 1 until n) x[n - i - 1] -= tmp[n - i] * x[n - i] // Backsubstitution.
    return x
}


///**
// * https://www.stkent.com/2015/07/03/building-smooth-paths-using-bezier-curves.html
// *
// * Computes a Poly-Bezier curve passing through a given list of knots.
// * The curve will be twice-differentiable everywhere and satisfy natural
// * boundary conditions at both ends.
// *
// * @param points a list of knots
// * @return      a Path representing the twice-differentiable curve
// * passing through all the given knots
// */
//fun smoothCurve(points: List<Vector2>, closed: Boolean = false): ShapeContour {
//    throwExceptionIfInputIsInvalid(points)
//    val builder = ContourBuilder(false)
//    val firstKnot: Vector2 = points[0]
//    builder.moveTo(firstKnot.x, firstKnot.y)
//
//    // number of Bezier curves we will join together
//    val n = points.size - 1
//    if (n == 1) {
//        val lastKnot: Vector2 = points[1]
//        builder.lineTo(lastKnot.x, lastKnot.y)
//    } else {
//        val controlPoints: Array<Vector2> = computeControlPoints(n, points)
//        for (i in 0 until n) {
//            val targetKnot: Vector2 = points[i + 1]
//            builder.appendCurve(controlPoints[i], controlPoints[n + i], targetKnot)
//        }
//    }
//    if (closed) builder.close()
//    return builder.result.first()
//}
//
//private fun computeControlPoints(n: Int, knots: List<Vector2>): Array<Vector2> {
//    val result: Array<Vector2> = Array(2 * n) { Vector2.ZERO }
//    val target: Array<Vector2> = constructTargetVector(n, knots)
//    val lowerDiag = constructLowerDiagonalVector(n - 1)
//    val mainDiag = constructMainDiagonalVector(n)
//    val upperDiag = constructUpperDiagonalVector(n - 1)
//    val newTarget: Array<Vector2> = Array(n) { Vector2.ZERO }
//    val newUpperDiag = DoubleArray(n - 1)
//
//    // forward sweep for control points c_i,0:
//    newUpperDiag[0] = upperDiag[0] / mainDiag[0]
//    newTarget[0] = target[0] * (1 / mainDiag[0])
//    for (i in 1 until n - 1) {
//        newUpperDiag[i] = upperDiag[i] / (mainDiag[i] - lowerDiag[i - 1] * newUpperDiag[i - 1])
//    }
//    for (i in 1 until n) {
//        val targetScale = 1 / (mainDiag[i] - lowerDiag[i - 1] * newUpperDiag[i - 1])
//        newTarget[i] = target[i] - (newTarget[i - 1] * lowerDiag[i - 1]) * targetScale
//    }
//
//    // backward sweep for control points c_i,0:
//    result[n - 1] = newTarget[n - 1]
//    for (i in n - 2 downTo 0) {
//        result[i] = newTarget[i] - (result[i + 1] * newUpperDiag[i])
//    }
//
//    // calculate remaining control points c_i,1 directly:
//    for (i in 0 until n - 1) {
//        result[n + i] = (knots[i + 1] * 2.0) - result[i + 1]
//    }
//    result[2 * n - 1] = (knots[n] + result[n - 1]) * 0.5
//    return result
//}
//
//private fun constructTargetVector(n: Int, knots: List<Vector2>): Array<Vector2> {
//    val result: Array<Vector2> = Array(n) { Vector2.ZERO }
//    result[0] = knots[0] + (knots[1] * 2.0)
//    for (i in 1 until n - 1) {
//        result[i] = ((knots[i] * 2.0) + knots[i + 1]) * 2.0
//    }
//    result[result.lastIndex] = (knots[n - 1] * 8.0) + knots[n]
//    return result
//}
//
//private fun constructLowerDiagonalVector(length: Int): DoubleArray {
//    val result = DoubleArray(length) { 1.0 }
//    result[result.lastIndex] = 2.0
//    return result
//}
//
//private fun constructMainDiagonalVector(n: Int): DoubleArray {
//    val result = DoubleArray(n)
//    result[0] = 2.0
//    for (i in 1 until result.size - 1) {
//        result[i] = 4.0
//    }
//    result[result.lastIndex] = 7.0
//    return result
//}
//
//private fun constructUpperDiagonalVector(length: Int) = DoubleArray(length) { 1.0 }
//
//private fun ContourBuilder.appendCurve(
//        control1: Vector2,
//        control2: Vector2,
//        targetPoint: Vector2
//) {
//    this.curveTo(control1, control2, targetPoint)
//}
//
//private fun throwExceptionIfInputIsInvalid(knots: Collection<Vector2>) {
//    require(knots.size >= 2) { "Collection must contain at least two knots" }
//}
