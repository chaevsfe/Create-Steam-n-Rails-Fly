#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

const vec4 Half = vec4(0.5);
const vec4 One = vec4(1.0);
const vec4 Two = vec4(2.0);

const float Pi = 3.1415926535;
const float PincushionAmount = 0.02;
const float CurvatureAmount = 0.02;
const float ScanlineAmount = 0.8;
const float ScanlineScale = 1.0;
const float ScanlineHeight = 1.0;
const float ScanlineBrightScale = 1.0;
const float ScanlineBrightOffset = 0.0;
const float ScanlineOffset = 0.0;
const vec3 Floor = vec3(0.05, 0.05, 0.05);
const vec3 Power = vec3(0.8, 0.8, 0.8);

out vec4 fragColor;

void main() {
    vec4 inTexel = texture(InSampler, texCoord);

    vec2 pinUnitCoord = texCoord * Two.xy - One.xy;
    float pincushionR2 = pow(length(pinUnitCoord), 2.0);
    vec2 pincushionCurve = pinUnitCoord * PincushionAmount * pincushionR2;
    vec2 scanCoord = texCoord;

    scanCoord *= One.xy - PincushionAmount * 0.2;
    scanCoord += PincushionAmount * 0.1;
    scanCoord += pincushionCurve;

    vec2 curvatureClipCurve = pinUnitCoord * CurvatureAmount * pincushionR2;
    vec2 screenClipCoord = texCoord;
    screenClipCoord -= Half.xy;
    screenClipCoord *= One.xy - CurvatureAmount * 0.2;
    screenClipCoord += Half.xy;
    screenClipCoord += curvatureClipCurve;

    if (scanCoord.x < 0.0) discard;
    if (scanCoord.y < 0.0) discard;
    if (scanCoord.x > 1.0) discard;
    if (scanCoord.y > 1.0) discard;

    float innerSine = scanCoord.y * InSize.y * ScanlineScale * 0.25;
    float scanBrightMod = sin(innerSine * Pi + ScanlineOffset * InSize.y * 0.25);
    float scanBrightness = mix(1.0, (pow(scanBrightMod * scanBrightMod, ScanlineHeight) * ScanlineBrightScale + 1.0) * 0.5 + ScanlineBrightOffset, ScanlineAmount);
    vec3 scanlineTexel = inTexel.rgb * scanBrightness;

    scanlineTexel = Floor + (One.xyz - Floor) * scanlineTexel;
    scanlineTexel.rgb = pow(scanlineTexel.rgb, Power);

    fragColor = vec4(scanlineTexel.rgb, 1.0);
}
