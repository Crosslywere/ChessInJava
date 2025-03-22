package com.crossly.engine.graphics;

import com.crossly.engine.Engine;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

public class ImageTexture extends Texture {

	public ImageTexture(String filepath, boolean flip, boolean pixelated) {
		super();
		int[] width = new int[1];
		int[] height = new int[1];
		int[] channels = new int[1];
		stbi_set_flip_vertically_on_load(flip);
		ByteBuffer data = stbi_load(Engine.getAbsolutePath(filepath), width, height, channels, 0);
		if (data == null)
			throw new RuntimeException("Image loading failed\n" + stbi_failure_reason());
		super.width = width[0];
		super.height = height[0];
		glBindTexture(GL_TEXTURE_2D, textureId);
		int format = GL_RGBA;
		switch (channels[0]) {
			case 1 -> format = GL_RED;
			case 2 -> format = GL_RG;
			case 3 -> format = GL_RGB;
		}
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, super.width, super.height, 0, format, GL_UNSIGNED_BYTE, data);
		stbi_image_free(data);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, pixelated ? GL_NEAREST : GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, pixelated ? GL_NEAREST : GL_LINEAR);
		glGenerateMipmap(GL_TEXTURE_2D);
	}
}
