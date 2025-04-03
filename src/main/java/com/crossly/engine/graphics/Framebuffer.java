package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public abstract class Framebuffer {

	protected int framebufferId;
	private final Texture frameImage;
	private final int width;
	private final int height;

	private static final Mesh SCREEN_MESH;
	private static final Shader SCREEN_SHADER;

	static {
		SCREEN_MESH = new Mesh(
				new float[] {
						-1f,  1f,
						-1f, -1f,
						 1f, -1f,
						 1f,  1f
				},
				new float[] {
						0f, 1f,
						0f, 0f,
						1f, 0f,
						1f, 1f,
				},
				null,
				new int[] {
						0, 1, 2,
						2, 3, 0,
				},
				false
		);
		SCREEN_SHADER = new Shader(
				"""
						#version 330 core
						layout (location = 0) in vec3 a_Pos;
						layout (location = 1) in vec2 a_TexCoord;
						out vec2 i_TexCoord;
						void main() {
							i_TexCoord = a_TexCoord;
							gl_Position = vec4(a_Pos, 1.0);
						}
						""",
				"""
						#version 330 core
						layout (location = 0) out vec4 o_Color;
						uniform sampler2D u_Texture;
						in vec2 i_TexCoord;
						void main() {
							o_Color = texture2D(u_Texture, i_TexCoord);
						}
						""",
				false
		);
	}

	public Framebuffer(int width, int height) {
		this.width = width;
		this.height = height;
		framebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		frameImage = new RenderTexture(width, height, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, frameImage.textureId, 0);
	}

	public Framebuffer(Texture frameImage, int attachment, int textureTarget) {
		this.width = frameImage.getWidth();
		this.height = frameImage.getHeight();
		framebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		this.frameImage = frameImage;
		glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, textureTarget, frameImage.textureId, 0);
	}

	protected final void checkStatus() {
		bind();
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer incomplete!");
	}

	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
	}

	public static void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void bindTexture(int index) {
		frameImage.bind(index);
	}

	public abstract void clearData();

	public final void clear() {
		clearScreen();
		clearData();
	}

	public static void setClearColor(float r, float g, float b) {
		glClearColor(r, g, b, 1f);
	}

	public void drawToScreen() {
		SCREEN_SHADER.use();
		SCREEN_SHADER.setInt("u_Texture", 0);
		bindTexture(0);
		SCREEN_MESH.draw();
	}

	public void delete() {
		glDeleteFramebuffers(framebufferId);
		frameImage.delete();
	}

	public static void clearScreen() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	protected final int getWidth() {
		return width;
	}

	protected final int getHeight() {
		return height;
	}
}
