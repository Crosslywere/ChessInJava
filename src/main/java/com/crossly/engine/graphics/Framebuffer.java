package com.crossly.engine.graphics;

import static org.lwjgl.opengl.GL11.*;

public class Framebuffer {

	public static void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	public static void setClearColor(float r, float g, float b) {
		glClearColor(r, g, b, 1f);
	}
}
