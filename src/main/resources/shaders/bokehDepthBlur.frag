#version 330

in vec2 v_texCoord0;

uniform sampler2D tex0;
uniform sampler2D depthBuffer;
uniform vec2 pixelScale;
uniform float near;
uniform float far;
uniform float focusPoint;
uniform float focusScale;

out vec4 o_color;

const float GOLDEN_ANGLE = 2.39996323;
const float MAX_BLUR_SIZE = 20.0;
const float RAD_SCALE = 0.5; // Smaller = nicer blur, larger = faster

// from http://tuxedolabs.blogspot.com/2018/05/bokeh-depth-of-field-in-single-pass.html

float linearDepth(vec2 uv) {
    float z = texture(depthBuffer, uv).x;
    return (2.0 * near) / (far + near - z * (far - near));
}

float getBlurSize(float depth, float focusPoint, float focusScale) {
    float coc = clamp((1.0 / focusPoint - 1.0 / depth) * focusScale, -1.0, 1.0);
    return abs(coc) * MAX_BLUR_SIZE;
}

void main() {
    float centerDepth = linearDepth(v_texCoord0);
    float centerSize = getBlurSize(centerDepth, focusPoint, focusScale);
    vec3 color = texture(tex0, v_texCoord0).rgb;
    float total = 1.0;

    float radius = RAD_SCALE;
    for (float angle = 0.0; radius < MAX_BLUR_SIZE; angle += GOLDEN_ANGLE) {
        vec2 tc = v_texCoord0 + vec2(cos(angle), sin(angle)) * pixelScale * radius;
        vec3 sampleColor = texture(tex0, tc).rgb;
        float sampleDepth = linearDepth(tc);
        float sampleSize = getBlurSize(sampleDepth, focusPoint, focusScale);
        if (sampleDepth > centerDepth) {
            sampleSize = clamp(sampleSize, 0.0, centerSize * 2.0);
        }
        float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
        color += mix(color/total, sampleColor, m);
        total += 1.0;
        radius += RAD_SCALE/radius;
    }
    color /= total;
    o_color.rgb = color; o_color.a = 1.0;

    //test render depth buffer
//    float z = linearDepth(v_texCoord0);
//    o_color.rgb = vec3(z);
//    o_color.a = 1.0;
}

