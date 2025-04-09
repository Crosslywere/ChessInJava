package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class IdFramebuffer extends Framebuffer {

	private final Renderbuffer idRenderBuffer;
	private final Renderbuffer depthStencilBuffer;

	public IdFramebuffer(int width, int height) {
		super(width, height);
		idRenderBuffer = new Renderbuffer(width, height, GL_R32I);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_RENDERBUFFER, idRenderBuffer.renderbufferId);
		depthStencilBuffer = new Renderbuffer(width, height, GL_DEPTH24_STENCIL8);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer.renderbufferId);
		checkStatus();
		unbind();
	}

	public void bind() {
		super.bind();
		glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
	}

	public void clearData() {
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		clearData(-1);
	}

	public void clearData(int value) {
		glClearBufferiv(GL_COLOR, 1, new int[]{value});
	}

	public void delete() {
		super.delete();
		idRenderBuffer.delete();
	}

	public int getId(int x, int y) {
		if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
			return -1;
		}
		int[] data = new int[1];
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferId);
		glReadBuffer(GL_COLOR_ATTACHMENT1);
		glReadPixels(x, y, 1, 1, GL_RED_INTEGER, GL_INT, data);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		return data[0];
	}
}
