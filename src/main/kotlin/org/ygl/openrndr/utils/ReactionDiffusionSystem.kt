package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BufferTexture
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.ColorType
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.math.Vector2
import org.ygl.openrndr.utils.PingPongBuffer

// https://www.karlsims.com/rd.html
@Description("Reaction Diffusion")
class ReactionDiffusionSystem(
        val width: Int,
        val height: Int
) {

    @DoubleParameter("diffusion A", 0.8, 1.2)
    var diffusionA = 1.0

    @DoubleParameter("diffusion B", 0.4, 0.6)
    var diffusionB = 0.5

    @DoubleParameter("feed", 0.001, 0.09)
    var feed = 0.055

    @DoubleParameter("kill", 0.03, 0.07)
    var kill = 0.062

    @DoubleParameter("dt", 0.0, 2.0)
    var dt = 1.0

    //TODO: figure out how to increase scale
    //@Vector2Parameter("scale")
    //var scale = Vector2(1.0, 1.0)

    @IntParameter("iterations", 1, 100)
    var iterations = 1

    @BooleanParameter("edgeWrap")
    var edgeWrap = true

    val chemicalAColor = ColorRGBa.RED
    val chemicalBColor = ColorRGBa.GREEN

    // TODO: add a texture for obstacles
    val buffer = PingPongBuffer(width, height, type = ColorType.FLOAT32, format = ColorFormat.RG)
    val renderTarget = renderTarget(width, height) { }

    private var outputColors: BufferTexture = bufferTextureFromColorList(listOf(ColorRGBa.WHITE, ColorRGBa.BLACK))

    init {
        buffer.fill(chemicalAColor)
    }

    fun setOutputColors(colors: List<ColorRGBa>) {
        outputColors = bufferTextureFromColorList(colors)
    }

    fun setCoralParams() {
        feed = 0.0545
        kill = 0.062
    }

    fun setMitosisParams() {
        feed = 0.0367
        kill = 0.0649
    }

    fun setWormParams() {
        feed = 0.039
        kill = 0.062
    }

    private fun bufferTextureFromColorList(colors: List<ColorRGBa>): BufferTexture {
        val bt = BufferTexture.create(colors.size * ColorFormat.RGBa.componentCount, ColorFormat.RGBa, ColorType.FLOAT32)
        bt.put {
            for (color in colors) {
                write(color)
            }
        }
        return bt
    }

    fun clear() {
        buffer.fill(chemicalAColor)
    }

    fun addReagent(drawer: Drawer, block: Drawer.() -> Unit) {
        renderTarget.attach(buffer.src())
        drawer.isolatedWithTarget(renderTarget) {
            block()
        }
        renderTarget.detachColorAttachments()
    }

    fun update(drawer: Drawer) {

        repeat(iterations) {
            renderTarget.attach(buffer.dst())
            drawer.isolatedWithTarget(renderTarget) {
                shadeStyle = shadeStyle {
                    fragmentTransform = """
                    vec2 pos = vec2(c_boundsPosition.x, 1 - c_boundsPosition.y);
                    
                    vec2 col0;
                    vec2 col1;
                    vec2 col2;
                    vec2 col3;
                    vec2 col4;
                    vec2 col5;
                    vec2 col6;
                    vec2 col7;
                    vec2 col8;
                    
                    if (p_wrap) {
                        float dx = 1.0 / u_viewDimensions.x;
                        float dy = 1.0 / u_viewDimensions.y;
                        
                        col0 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(-dx, -dy))).rg;
                        col1 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(  0, -dy))).rg;
                        col2 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(+dx, -dy))).rg;
                        col3 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(-dx,  0))).rg;
                        col4 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(  0,  0))).rg;
                        col5 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(+dx,  0))).rg;
                        col6 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(-dx, +dy))).rg;
                        col7 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(  0, +dy))).rg;
                        col8 = texture(p_tex, fract(pos + vec2(1, 1) + vec2(+dx, +dy))).rg;
                    } else {
                    
                        col0 = textureOffset(p_tex, pos, ivec2(-1, -1)).rg;
                        col1 = textureOffset(p_tex, pos, ivec2( 0, -1)).rg;
                        col2 = textureOffset(p_tex, pos, ivec2(+1, -1)).rg;
                        col3 = textureOffset(p_tex, pos, ivec2(-1,  0)).rg;
                        col4 = textureOffset(p_tex, pos, ivec2( 0,  0)).rg;
                        col5 = textureOffset(p_tex, pos, ivec2(+1,  0)).rg;
                        col6 = textureOffset(p_tex, pos, ivec2(-1, +1)).rg;
                        col7 = textureOffset(p_tex, pos, ivec2( 0, +1)).rg;
                        col8 = textureOffset(p_tex, pos, ivec2(+1, +1)).rg;                    
                    }
                    
                    vec2 laplace = (
                        0.05 * col0 + 0.2 * col1 + 0.05 * col2 +
                        0.20 * col3 - 1.0 * col4 + 0.20 * col5 +
                        0.05 * col6 + 0.2 * col7 + 0.05 * col8
                    );
                    
                    float rate = col4.r * col4.g * col4.g;
                    float du = p_dA * laplace.r - rate + p_feed * (1.0 - col4.r);
                    float dv = p_dB * laplace.g + rate - (p_feed + p_kill) * col4.g;
                    
                    float u = clamp(col4.r + du * p_dt, 0.0, 1.0); 
                    float v = clamp(col4.g + dv * p_dt, 0.0, 1.0);
                    x_fill.rg = vec2(u, v);
                    //x_fill.rg = vec4(u, v, 0.0, 1.0);
                """.trimIndent()
                    parameter("tex", buffer.src())
                    parameter("kill", kill)
                    parameter("feed", feed)
                    parameter("dA", diffusionA)
                    parameter("dB", diffusionB)
                    parameter("dt", dt)
                    parameter("wrap", edgeWrap)
                }
                stroke = null
                rectangle(renderTarget.colorBuffer(0).bounds)
            }
            renderTarget.detachColorAttachments()
            buffer.swap()
        }
    }

    fun render(drawer: Drawer, scale: Vector2 = Vector2.ONE, offset: Vector2 = Vector2.ZERO) {
        drawer.isolated {
            shadeStyle = shadeStyle {
                fragmentPreamble = """
                    vec4 getColor(float x) {
                        int size = ${outputColors.elementCount / outputColors.format.componentCount};
                        float p = size * clamp(x, 0, 1);
                        int index = int(p);
                        int max = size - 1;
                        if (index >= max) return texelFetch(p_colorMap, max); 
                        vec4 c1 = texelFetch(p_colorMap, index);
                        vec4 c2 = texelFetch(p_colorMap, index + 1);
                        return mix(c1, c2, p - index);
                    }
                """.trimIndent()

                fragmentTransform = """
                    vec2 pos = vec2(c_boundsPosition.x, 1 - c_boundsPosition.y);
                    pos = pos / p_scale + p_offset;
                    vec2 uv = texture(p_texture, pos).rg;
                    //float c = smoothstep(0.02, 0.98, uv.r - uv.g);
                    //float c = smoothstep(0.0, 1.0, uv.r - uv.g);
                    float c = clamp(uv.r - uv.g, 0, 1);
                    x_fill = getColor(c);
                """.trimIndent()
                parameter("scale", scale)
                parameter("offset", offset / buffer.src().bounds.dimensions)
                parameter("texture", buffer.src())
                parameter("colorMap", outputColors)
            }
            rectangle(buffer.src().bounds)
        }
    }

}
