uniform sampler2D u_texture;

varying highp vec4 v_color;
varying highp vec2 v_texCoord;

void main() {
	highp float smoothing = 1.0/16.0;
    highp float distance = texture2D(u_texture, v_texCoord).a;
    highp float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    gl_FragColor = vec4(v_color.rgb, alpha);
}