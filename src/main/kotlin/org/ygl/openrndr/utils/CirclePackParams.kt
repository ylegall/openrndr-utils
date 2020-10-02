package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle

class CirclePackParams(
        val bounds: Rectangle,
        val minRadius: Double = 10.0,
        val maxRadius: Double = 100.0,
        val spatialMap: SpatialMap<Circle> = TileGrid(bounds.offsetEdges(maxRadius, maxRadius),
                (minRadius + maxRadius)/2.0,
                (minRadius + maxRadius)/2.0
        ),
        val wrap: Boolean = false,
        val padding: Double = 0.0,
        val circleLimit: Int? = null,
        val targetFillRatio: Double? = null,
        val maskFunction: (Vector2) -> Boolean = { true }
)
