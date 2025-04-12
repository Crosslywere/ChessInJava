package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class Renderbuffer {

	protected int renderbufferId;

	public Renderbuffer(int width, int height, int internalFormat) {
		renderbufferId = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderbufferId);
		glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, width, height);
	}

	public int getRenderbufferId() {
		return renderbufferId;
	}

	public void delete() {
		glDeleteRenderbuffers(renderbufferId);
	}
}
