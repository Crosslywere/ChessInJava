package com.crossly.engine.window;

import com.crossly.engine.Engine;
import com.crossly.engine.input.Input;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Window {

	private final long window;

	public Window(Engine engine, Input input) {
		if (!glfwInit())
			throw new RuntimeException("glfwInit failed!");
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		if (!engine.getWindowResizable())
			glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		window = glfwCreateWindow(engine.getWindowWidth(), engine.getWindowHeight(), engine.getWindowTitle(), 0L, 0L);
		if (window == 0)
			throw new RuntimeException("glfwCreateWindow failed!");
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		GL.createCapabilities();
		input.setWindowHandle(window);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glBlendEquation(GL_FUNC_ADD);
		glfwSetKeyCallback(window, new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				input.setKeyPressed(key, action >= GLFW_PRESS);
			}
		});
		glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				input.setButtonPressed(button, action >= GLFW_PRESS);
			}
		});
		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double x, double y) {
				input.setMousePos(x, y);
			}
		});
		glfwSetScrollCallback(window, new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double x, double y) {
				input.setScrollAmount(x, y);
			}
		});
		glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				engine.setWindowWidth(width);
				engine.setWindowHeight(height);
				engine.onResize();
				glViewport(0, 0, width, height);
			}
		});
	}

	public boolean isOpen() {
		glfwSwapBuffers(window);
		glfwPollEvents();
		return !glfwWindowShouldClose(window);
	}

	public void cleanup() {
		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
