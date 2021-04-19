#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform float filterYes;
uniform float _filterYes;
uniform samplerExternalOES s_texture;

vec4 inverse(vec4 color) {
    return abs(vec4(color.rgb - 1.0, color.a));
}

vec4 blackAndWhite(vec4 color) {
    return vec4(vec3(1.0, 1.0, 1.0) * (color.r + color.g + color.b) / 3.0, color.a);
}

void main() {
    vec4 color = texture2D( s_texture, textureCoordinate );
    if(filterYes == _filterYes) {
        gl_FragColor = color;
    } else {
        gl_FragColor = inverse(color);
        gl_FragColor = blackAndWhite(color);
    }
}