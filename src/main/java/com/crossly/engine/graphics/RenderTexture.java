package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL33.*;

public class RenderTexture extends Texture {

	public RenderTexture(int width, int height, int internalFormat, int format, int type) {
		super();
		super.width = width;
		super.height = height;
		glBindTexture(GL_TEXTURE_2D, super.textureId);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
}
