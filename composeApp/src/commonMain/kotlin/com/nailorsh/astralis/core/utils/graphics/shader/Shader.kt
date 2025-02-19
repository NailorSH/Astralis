package com.nailorsh.astralis.core.utils.graphics.shader

fun makeSRGBUtilsShader(): String {
    return """
        float srgbToLinear(float srgb)
        {
            float s = step(float(0.04045), srgb);
            float d = 1. - s;
            return s * pow((srgb+0.055)/1.055, float(2.4)) +
                   d * srgb/12.92;
        }
    
        float linearToSRGB(float lin)
        {
            float s = step(float(0.0031308), lin);
            float d = 1. - s;
            return s * (1.055*pow(lin, float(1./2.4))-0.055) +
                   d *  12.92*lin;
        }
    
        vec3 srgbToLinear(vec3 srgb)
        {
            vec3 s = step(vec3(0.04045), srgb);
            vec3 d = vec3(1) - s;
            return s * pow((srgb+0.055)/1.055, vec3(2.4)) +
                   d * srgb/12.92;
        }
    
        vec3 linearToSRGB(vec3 lin)
        {
            vec3 s = step(vec3(0.0031308), lin);
            vec3 d = vec3(1) - s;
            return s * (1.055*pow(lin, vec3(1./2.4))-0.055) +
                   d *  12.92*lin;
        }
    """.trimIndent()
}
