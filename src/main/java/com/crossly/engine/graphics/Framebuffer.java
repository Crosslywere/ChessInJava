package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class Framebuffer {

	private final int framebufferId;
	private final RenderTexture renderTexture;
	private final Renderbuffer renderIdBuffer;
	private final Renderbuffer renderDepthStencilBuffer;
	private final int width, height;

	private static final Mesh SCREEN_MESH;
	private static final Shader SCREEN_SHADER;
	static {
		SCREEN_MESH = new Mesh(
				new float[]{
						-1f,-1f,
						1f,-1f,
						1f, 1f,
						-1f, 1f,
				},
				new float[]{
						0f, 0f,
						1f, 0f,
						1f, 1f,
						0f, 1f,
				},
				null,
				new int[]{
						0, 1, 2, 2, 3, 0
				}, false);
		SCREEN_SHADER = new Shader(
				"""
					#version 330 core
					layout (location = 0) in vec2 aP;
					layout (location = 1) in vec2 aT;
					out vec2 tc;
					void main() {
						tc = aT;
						gl_Position = vec4(aP, 0., 1.);
					}""",
				"""
					#version 330 core
					uniform sampler2D st;
					in vec2 tc;
					void main() {
						gl_FragColor = texture2D(st, tc);
					}""",
				false);
	}

	public Framebuffer(int width, int height) {
		this.width = width;
		this.height = height;
		framebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		renderTexture = new RenderTexture(width, height, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTexture.textureId, 0);
		renderIdBuffer = new Renderbuffer(width, height, GL_R32I);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_RENDERBUFFER, renderIdBuffer.renderbufferId);
		renderDepthStencilBuffer = new Renderbuffer(width, height, GL_DEPTH24_STENCIL8);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderDepthStencilBuffer.renderbufferId);
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer is incomplete!");
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void clearIdBuffer(int value) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		glClearBufferiv(GL_COLOR, 1, new int[]{value});
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void delete() {
		renderTexture.delete();
		renderIdBuffer.delete();
		renderDepthStencilBuffer.delete();
		glDeleteFramebuffers(framebufferId);
	}

	public RenderTexture getRenderTexture() {
		return renderTexture;
	}

	public int getId(int x, int y) {
		int[] data = new int[1];
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferId);
		glReadBuffer(GL_COLOR_ATTACHMENT1);
		glReadPixels(x, y, 1, 1, GL_RED_INTEGER, GL_INT, data);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		return data[0];
	}

	public static void drawTo(Framebuffer framebuffer) {
		framebuffer.clearIdBuffer(-1);
		glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
	}

	public static void renderToScreen(Framebuffer framebuffer) {
		clear();
		SCREEN_SHADER.use();
		framebuffer.renderTexture.bind(0);
		SCREEN_SHADER.setInt("st", 0);
		SCREEN_MESH.draw();
	}

	public static void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public static void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	public static void setClearColor(float r, float g, float b) {
		glClearColor(r, g, b, 1f);
	}
}
