package com.crossly.engine.graphics;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL33.*;

public class Mesh {

//	private final int vertexArray;
//	private final ArrayList<Integer> bufferObjects = new ArrayList<>();
//	private final int elementCount;
//
//	public Mesh(float[] positionData, float[] texCoordinateData, float[] normalData, float[] colorData, int[] indicesData, boolean is3D) {
//		vertexArray = glGenVertexArrays();
//		glBindVertexArray(vertexArray);
//		elementCount = indicesData.length;
//		bufferObjects.add(glGenBuffers());
//		glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.getLast());
//		glBufferData(GL_ARRAY_BUFFER, positionData, GL_STATIC_DRAW);
//		glVertexAttribPointer(0, is3D ? 3 : 2, GL_FLOAT, false, 0, 0L);
//		glEnableVertexAttribArray(0);
//		if (texCoordinateData != null && texCoordinateData.length > 0) {
//			bufferObjects.add(glGenBuffers());
//			glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.getLast());
//			glBufferData(GL_ARRAY_BUFFER, texCoordinateData, GL_STATIC_DRAW);
//			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0L);
//			glEnableVertexAttribArray(1);
//		}
//		if (normalData != null && normalData.length > 0 && is3D) {
//			bufferObjects.add(glGenBuffers());
//			glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.getLast());
//			glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
//			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0L);
//			glEnableVertexAttribArray(2);
//		}
//		if (colorData != null && colorData.length > 0) {
//			bufferObjects.add(glGenBuffers());
//			glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.getLast());
//			glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
//			glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0L);
//			glEnableVertexAttribArray(3);
//		}
//		bufferObjects.add(glGenBuffers());
//		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObjects.getLast());
//		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesData, GL_STATIC_DRAW);
//	}

	private final int vertexArrayId;
	private final ArrayList<Integer> buffers = new ArrayList<>();
	private final int count;

	public Mesh(float[] positionData, float[] textureCoordinateData, float[] normalData, int[] indicesData, boolean is3D) {
		count = indicesData.length;
		vertexArrayId = glGenVertexArrays();
		glBindVertexArray(vertexArrayId);
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, positionData, GL_STATIC_DRAW);
		glVertexAttribPointer(0, is3D ? 3 : 2, GL_FLOAT, false, 0, 0L);
		glEnableVertexAttribArray(0);
		buffers.add(vbo);
		if (textureCoordinateData != null && textureCoordinateData.length > 0) {
			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, textureCoordinateData, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0L);
			glEnableVertexAttribArray(1);
			buffers.add(vbo);
		}
		if (normalData != null && normalData.length > 0) {
			vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0L);
			glEnableVertexAttribArray(2);
			buffers.add(vbo);
		}
		int ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesData, GL_STATIC_DRAW);
		buffers.add(ebo);
	}

	public void draw() {
		glBindVertexArray(vertexArrayId);
		glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0L);
	}

	public void delete() {
		buffers.forEach(GL33::glDeleteBuffers);
		glDeleteVertexArrays(vertexArrayId);
	}
}
