package org.ygl.openrndr.utils

import org.openrndr.Application
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.DepthBuffer
import org.openrndr.draw.Filter
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.filterShaderFromUrl
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.math.Vector2


// http://tuxedolabs.blogspot.com/2018/05/bokeh-depth-of-field-in-single-pass.html
@Description("bokeh depth of field")
class BokehDepthBlur: Filter(
        filterShaderFromUrl(Application::class.java.getResource("/shaders/bokehDepthBlur.frag").toExternalForm())
) {
    @DoubleParameter("near", 0.0, 10.0)
    var near: Double by parameters

    @DoubleParameter("far", 0.0, 9000.0, precision = 1)
    var far: Double by parameters

    @DoubleParameter("focus scale", 0.0, 1.0)
    var focusScale: Double by parameters

    @DoubleParameter("focus point", 0.0, 1.0, precision = 2)
    var focusPoint: Double by parameters

    @Vector2Parameter("pixel scale")
    var pixelScale: Vector2 by parameters

    var depthBuffer: DepthBuffer by parameters

    private var intermediate: ColorBuffer? = null

    init {
        far = 1000.0
        near = 0.1
        focusScale = 0.5
        focusPoint = 0.5
        pixelScale = Vector2.ZERO
    }

    override fun apply(source: Array<ColorBuffer>, target: Array<ColorBuffer>) {
        intermediate?.let {
            if (it.width != target[0].width || it.height != target[0].height) {
                intermediate = null
            }
        }

        if (intermediate == null) {
            intermediate = colorBuffer(target[0].width, target[0].height, target[0].contentScale, target[0].format, target[0].type)
        }

        intermediate?.let {
            //depthBufferOut = depthBuffer
            pixelScale = Vector2(1.0 / intermediate!!.width, 1.0 / intermediate!!.height)
            super.apply(source, arrayOf(it))
            it.copyTo(target[0])
        }
    }
}