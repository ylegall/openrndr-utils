package org.ygl.openrndr.utils

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BufferTexture
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.ColorType
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter


fun colorMapBufferTexture(colorMap: ColorMap): BufferTexture {
    return colorMapBufferTexture(colorMap.colorIntervals)
}

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

@Description("material lighting")
class MaterialLightConfig(
        @DoubleParameter("specular", 0.0, 1.0)
        var specular: Double = 0.9,

        @DoubleParameter("diffuse", 0.0, 1.0)
        var diffuse: Double = 0.6,

        @DoubleParameter("ambient", 0.0, 1.0)
        var ambient: Double = 0.1,

        var objectColor: ColorRGBa = ColorRGBa(0.4, 0.5, 0.6),

        var lightColor: ColorRGBa = ColorRGBa(0.9, 0.8, 0.7),

        @DoubleParameter("shineness", 0.0, 1.0)
        var shineness: Double = 32.0,

        var diffuseTexture: ColorBuffer? = null,

        var specularTexture: ColorBuffer? = null
) {
    private fun ColorRGBa.glslString() = "vec4($r, $g, $b, $a)"

    fun combined() = """
        float textureBrightness(sampler2D image) {
            vec4 c = texture(image, va_texCoord0.xy);
            return 0.299 * c.r + 0.587 * c.g + 0.114 * c.b;
        }
        
        vec4 diffuse(vec3 lightDir) {
            vec4 lightColor = ${lightColor.glslString()};
            //float diff = max(dot(v_worldNormal, lightDir), 0.0);
            float diff = max(dot(va_normal, lightDir), 0.0);
            ${if (diffuseTexture != null) "diff *= textureBrightness(p_diffuseTexture);" else "" }
            return $diffuse * diff * lightColor;
        }
        
        vec4 specular(vec3 lightDir) {
            vec4 lightColor = ${lightColor.glslString()};
            vec3 viewPos = p_viewPos;
            //vec3 viewPos = (inverse(u_viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
            vec3 viewDir = normalize(viewPos - va_position);
            vec3 reflectDir = reflect(-lightDir, va_normal);
            //vec3 reflectDir = reflect(-lightDir, v_worldNormal);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), $shineness);
            ${if (specularTexture != null) "spec *= textureBrightness(p_specTexture);" else ""}
            return $specular * spec * lightColor;
        }
        
        vec4 combined_lighting(vec3 lightPos) {
            vec4 lightColor = ${lightColor.glslString()};
            vec4 objectColor = ${objectColor.glslString()};
            vec3 lightDir = normalize(lightPos - va_position);
            vec4 ambi = $ambient * lightColor;
            vec4 diff = diffuse(lightDir);
            vec4 spec = specular(lightDir);
            vec3 result = ((spec + ambi + diff) * objectColor).rgb;
            return vec4(result, 1);
        }
    """.trimIndent()
}

val shaderLights = """
    float getLight(vec3 light) {
        float dot_prod = dot(-va_normal, light);
        float normalized = (1 + dot_prod) / 2;
        return clamp(normalized, 0.0, 1.0);
    }
    
    float getLight(vec3 light, vec3 normal) {
        float dot_prod = dot(-normal, light);
        float normalized = (1 + dot_prod) / 2;
        return clamp(normalized, 0.0, 1.0);
    }
    
    vec4 diffuse(vec3 lightDir, vec4 lightColor) {
        //return max(dot(v_worldNormal, lightDir), 0.0) * lightColor;
        return max(dot(va_normal, lightDir), 0.0) * lightColor;
    }
    
    vec4 specular(vec3 lightDir, vec4 lightColor, float specularStrength) {
        vec3 viewPos = p_viewPos;
        //vec3 viewPos = (inverse(u_viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
        vec3 viewDir = normalize(viewPos - va_position);
        vec3 reflectDir = reflect(-lightDir, va_normal);
        //vec3 reflectDir = reflect(-lightDir, v_worldNormal);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32); // 32 is the shininess
        return specularStrength * spec * lightColor;
    }
    
    vec3 combined_lighting(vec3 lightPos, vec4 lightColor, float ambientStrength, float specularStrength) {
        vec3 lightDir = normalize(lightPos - va_position);
        vec4 ambi = ambientStrength * lightColor;
        vec4 diff = diffuse(lightDir, lightColor);
        vec4 spec = specular(lightDir, lightColor, specularStrength);
        return (spec + ambi + diff).rgb;
    }
"""
