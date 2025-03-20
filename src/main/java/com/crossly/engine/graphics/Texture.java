package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public abstract class Texture {
	protected int textureId;
	protected int width;
	protected int height;

	protected Texture() {
		textureId = glGenTextures();
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public final void bind(int index) {
		glActiveTexture(GL_TEXTURE0 + index);
		glBindTexture(GL_TEXTURE_2D, textureId);
	}

	public final void delete() {
		glDeleteTextures(textureId);
	}
}
