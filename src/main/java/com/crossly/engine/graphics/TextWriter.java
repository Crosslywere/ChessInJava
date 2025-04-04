package com.crossly.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.*;

public class TextWriter {

	private final FontAtlas fontAtlas;
	private Matrix4f viewMatrix;
	private float lineHeight = 1.2f;

	private final static int VERTEX_ARRAY_ID;
	private final static int POS_VERTEX_BUFFER_ID;
	private final static int TEX_VERTEX_BUFFER_ID;
	private final static int ELEMENT_BUFFER_ID;
	private final static Shader FONT_SHADER;

	static {
		VERTEX_ARRAY_ID = glGenVertexArrays();
		glBindVertexArray(VERTEX_ARRAY_ID);
		POS_VERTEX_BUFFER_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, POS_VERTEX_BUFFER_ID);
		glBufferData(GL_ARRAY_BUFFER, 8 * Float.BYTES, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);
		glEnableVertexAttribArray(0);
		TEX_VERTEX_BUFFER_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, TEX_VERTEX_BUFFER_ID);
		glBufferData(GL_ARRAY_BUFFER, 8 * Float.BYTES, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0L);
		glEnableVertexAttribArray(1);
		ELEMENT_BUFFER_ID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ELEMENT_BUFFER_ID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] { 0, 1, 2, 2, 3, 0 }, GL_STATIC_DRAW);
		glBindVertexArray(0);

		FONT_SHADER = new Shader(
				"""
						#version 330 core
						layout (location = 0) in vec2 aPos;
						layout (location = 1) in vec2 aTexCoord;
						out vec2 texCoord;
						uniform mat4 projection;
						void main() {
							texCoord = aTexCoord;
							gl_Position = projection * vec4(aPos, 0., 1.);
						}
						""",
				"""
						#version 330 core
						layout (location = 0) out vec4 oColor;
						in vec2 texCoord;
						uniform sampler2D fontTexture;
						void main() {
							oColor = vec4(vec3(1.0), texture2D(fontTexture, texCoord).r);
						}
						""",
				false
		);
	}

	public TextWriter(FontAtlas fontAtlas, int viewWidth, int viewHeight) {
		this.fontAtlas = fontAtlas;
		setViewMatrix(viewWidth, viewHeight);
	}

	public TextWriter(FontAtlas fontAtlas) {
		this(fontAtlas, 1, 1);
	}

	public FontAtlas getFontAtlas() {
		return fontAtlas;
	}

	public float getLineHeight() {
		return lineHeight;
	}

	public void setLineHeight(float lineHeight) {
		this.lineHeight = lineHeight;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public void setViewMatrix(int viewWidth, int viewHeight) {
		this.viewMatrix = new Matrix4f().ortho(0f, viewWidth, viewHeight, 0f, 0f, 1f);
	}

	public void writeText(String text, Vector2f position, float size) {
		FONT_SHADER.use();
		FONT_SHADER.setMatrix4("projection", viewMatrix);
		FONT_SHADER.setFloat3("color", new Vector3f(1f));
		fontAtlas.bind(1);
		FONT_SHADER.setInt("fontTexture", 1);
		Vector2f origin = new Vector2f(position);
		for (var ch : text.toCharArray()) {
			position = writeCharacter(ch, position, origin, size);
		}
	}

	public Vector2f writeText(String text, Vector2f position, float size, Shader fontShader) {
		fontShader.use();
		Vector2f origin = new Vector2f(position);
		for (var ch : text.toCharArray()) {
			position = writeCharacter(ch, position, origin, size);
		}
		return position;
	}

	private Vector2f writeCharacter(char character, Vector2f position, Vector2f origin, float size) {
		if (character >= 32 && character <= 128) {
			var packedChar = fontAtlas.getPackedChar(character);
			var alignedQuad = fontAtlas.getAlignedQuad(character);
			Vector2f glyphSize = new Vector2f(
					(packedChar.x1() - packedChar.x0()) * (size / fontAtlas.getImportSize()),
					(packedChar.y1() - packedChar.y0()) * (size / fontAtlas.getImportSize())
			);
			Vector2f glyphBottomLeft = new Vector2f(
					(position.x + packedChar.xoff() * (size / fontAtlas.getImportSize())),
					position.y + (packedChar.yoff() + packedChar.y1() - packedChar.y0()) * (size / fontAtlas.getImportSize())
			);
			glBindVertexArray(VERTEX_ARRAY_ID);
			glBindBuffer(GL_ARRAY_BUFFER, POS_VERTEX_BUFFER_ID);
			glBufferSubData(GL_ARRAY_BUFFER, 0, new float[]{
					glyphBottomLeft.x + glyphSize.x, glyphBottomLeft.y - glyphSize.y,
					glyphBottomLeft.x, glyphBottomLeft.y - glyphSize.y,
					glyphBottomLeft.x, glyphBottomLeft.y,
					glyphBottomLeft.x + glyphSize.x, glyphBottomLeft.y,
			});
			glBindBuffer(GL_ARRAY_BUFFER, TEX_VERTEX_BUFFER_ID);
			glBufferSubData(GL_ARRAY_BUFFER, 0, new float[]{
					alignedQuad.s1(), alignedQuad.t0(),
					alignedQuad.s0(), alignedQuad.t0(),
					alignedQuad.s0(), alignedQuad.t1(),
					alignedQuad.s1(), alignedQuad.t1(),
			});
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ELEMENT_BUFFER_ID);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);
			return new Vector2f(position.x() + packedChar.xadvance() * (size / fontAtlas.getImportSize()), position.y());
		}
		else if (character == '\n') {
			float yoffset = (fontAtlas.getImportSize() * 2f * size) / fontAtlas.getImportSize();
			float p = size * 2f / fontAtlas.getImportSize();
			origin.y += lineHeight * (p * yoffset);
			return origin;
		}
		return position;
	}
}
