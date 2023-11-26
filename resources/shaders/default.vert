#version 330 core

layout (location = 0) in vec3 vLocal;
layout (location = 1) in vec3 vColor;

out vec3 vertexColor;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;

void main()
{
  vertexColor = vColor;
  gl_Position = projection * view * model * vec4(vLocal, 1.0);
}
