package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BufferTexture
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.ColorType

fun Int.argb2rgba() = ColorRGBa(
        r = ((this shr 16) and 0xFF) / 255.0,
        g = ((this shr 8) and 0xFF) / 255.0,
        b = (this and 0xFF) / 255.0,
        a = ((this shr 24) and 0xFF) / 255.0
)

fun <T: Number> color(r: T, g: T, b: T) = color(r, g, b, 255)

fun <T: Number> color(r: T, g: T, b: T, a: T) = ColorRGBa(
        r.toDouble() / 255,
        g.toDouble() / 255,
        b.toDouble() / 255,
        a.toDouble() / 255
)

fun colorMapBufferTexture(colors: List<ColorRGBa>): BufferTexture {
    val bt = BufferTexture.create(colors.size * ColorFormat.RGBa.componentCount, ColorFormat.RGBa, ColorType.FLOAT32)
    bt.put {
        for (color in colors) {
            write(color)
        }
    }
    return bt
}

fun shaderColorMap(colorMapSize: Int) = """
    vec4 getColor(float x) {
        float p = $colorMapSize * clamp(x, 0, 1);
        int idx = int(p);
        int max = $colorMapSize - 1;
        if (idx >= max) return texelFetch(p_colorMap, max); 
        vec4 c1 = texelFetch(p_colorMap, idx);
        vec4 c2 = texelFetch(p_colorMap, idx + 1);
        return mix(c1, c2, p - idx);
    }
"""

val shaderLights = """
    float getLight(vec3 light) {
        float dot_prod = dot(-va_normal, light);
        float normalized = (1 + dot_prod) / 2;
        return clamp(normalized, 0.0, 1.0);
    }
"""
