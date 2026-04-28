#version 150

#moj_import <fog.glsl>
#moj_import <arcana:psrddnoise2.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;
uniform vec3 TurbulenceColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
in vec2 localUV;

out vec4 fragColor;

void main() {
    vec2 g;
    vec3 gg;
    float scale = 3;
	float mod = psrddnoise(localUV * scale, vec2(scale), GameTime*20*60, g, gg);
    mod = psrddnoise(localUV * 2 * mod, vec2(scale), GameTime*20*60, g, gg);
    vec4 texCol = texture(Sampler0, texCoord0);
    vec3 mixed = mix(texCol.rgb, TurbulenceColor, clamp(mod/2, 0, 1));
    vec4 color = vec4(mixed.r, mixed.g, mixed.b, texCol.a) * vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
