package com.crossly.engine.graphics;

import com.crossly.engine.Engine;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBTruetype.*;

public class FontAtlas extends Texture {

	private final STBTTPackedchar.Buffer packedChars;
	private final STBTTAlignedQuad.Buffer alignedQuads;
	private final ByteBuffer imageData;
	private final float importSize;
	private final static int CHARS_TO_INCLUDE = 128 - 32;

	public FontAtlas(String fontPath, float importSize) {
		super();
		super.width = super.height = 1024;
		this.importSize = importSize;
		ByteBuffer ttfData;
		try {
			byte[] data = Files.readAllBytes(Paths.get(Engine.getAbsolutePath(fontPath)));
			ttfData = MemoryUtil.memAlloc(data.length);
			ttfData.put(data);
			ttfData.flip();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		imageData = MemoryUtil.memAlloc(width * height);
		try (STBTTPackContext ctx = STBTTPackContext.create()) {
			stbtt_PackBegin(ctx, imageData, width, height, 0, 2);
			packedChars = STBTTPackedchar.malloc(CHARS_TO_INCLUDE);
			stbtt_PackFontRange(ctx, ttfData, 0, this.importSize, 32, packedChars);
			stbtt_PackEnd(ctx);
		}
		alignedQuads = STBTTAlignedQuad.malloc(CHARS_TO_INCLUDE);
		for (int i = 0; i < CHARS_TO_INCLUDE; i++) {
			float[] x, y;
			x = y = new float[1];
			stbtt_GetPackedQuad(packedChars, width, height, i,  x, y, alignedQuads.get(i), true);
		}
		glBindTexture(GL_TEXTURE_2D, textureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, imageData);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public FontAtlas(String fontPath) {
		this(fontPath, 64f);
	}

	public float getImportSize() {
		return importSize;
	}

	public void writeToImage(String path) {
		stbi_write_png(path, width, height, 1, imageData, width);
	}

	public STBTTAlignedQuad getAlignedQuad(int i) {
		return alignedQuads.get(i);
	}

	public STBTTAlignedQuad getAlignedQuad(char ch) {
		return getAlignedQuad(ch - 32);
	}

	public STBTTPackedchar getPackedChar(int i) {
		return packedChars.get(i);
	}

	public STBTTPackedchar getPackedChar(char ch) {
		return getPackedChar(ch - 32);
	}
}
