package org.ygl.openrndr.utils

import org.openrndr.Program
import org.openrndr.draw.BlendMode
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorType
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extras.camera.OrbitalCamera
import org.openrndr.extras.camera.applyTo
import org.openrndr.math.Spherical
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Description("depth blur")
class DepthOfFieldBlur(
        program: Program,
        val camera: OrbitalCamera
) {
    private val imageTarget = renderTarget(program.width, program.height) { colorBuffer() }
    private val blurTarget = renderTarget(program.width, program.height) { colorBuffer(type = ColorType.FLOAT32) }

    @IntParameter("blur iterations", 0, 40)
    var blurIterations = 20

    @DoubleParameter("blur radius", 0.0, 5.0)
    var blurRadius = 0.5

    fun computeBlur(
            drawer: Drawer,
            drawFunction: (Drawer) -> Unit
    ) {
        val lookAt = camera.lookAt
        val sphericalEye = camera.spherical

        drawer.isolatedWithTarget(blurTarget) {
            drawStyle.blendMode = BlendMode.OVER
            camera.applyTo(drawer)
            drawFunction(drawer)
        }

        for (i in 0 until blurIterations) {
            val angle = 2 * PI * i / blurIterations
            val rx = blurRadius * cos(angle)
            val ry = blurRadius * sin(angle)
            val newPosition = sphericalEye + Spherical(rx, ry, 0.0)

            // first draw to opaque buffer
            drawer.isolatedWithTarget(imageTarget) {
                camera.setView(lookAt, newPosition, camera.fov)
                camera.applyTo(drawer)
                drawFunction(drawer)
            }

            // then copy to accumulation buffer
            drawer.isolatedWithTarget(blurTarget) {
                drawStyle.blendMode = BlendMode.ADD
                image(imageTarget.colorBuffer(0))
            }
        }

        // reset camera
        camera.setView(lookAt, sphericalEye, camera.fov)
    }

    fun blurResult(drawer: Drawer): ColorBuffer {
        drawer.isolatedWithTarget(imageTarget) {
            shadeStyle = shadeStyle {
                fragmentTransform = """
                x_fill.rgba /= p_iterations;
                """.trimIndent()
                parameter("iterations", blurIterations)
            }
            image(blurTarget.colorBuffer(0))
        }
        return imageTarget.colorBuffer(0)
    }
}

fun Program.depthOfFieldBlur(camera: OrbitalCamera) = DepthOfFieldBlur(this, camera)

