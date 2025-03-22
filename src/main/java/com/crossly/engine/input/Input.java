package com.crossly.engine.input;

import org.joml.Vector2i;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

	public static final int MOUSE_BUTTON_LEFT = GLFW_MOUSE_BUTTON_LEFT;
	public static final int MOUSE_BUTTON_RIGHT = GLFW_MOUSE_BUTTON_RIGHT;
	public static final int MOUSE_BUTTON_MIDDLE = GLFW_MOUSE_BUTTON_MIDDLE;
	public static final int MOUSE_BUTTON_FORWARD = GLFW_MOUSE_BUTTON_4;
	public static final int MOUSE_BUTTON_BACK = GLFW_MOUSE_BUTTON_5;

	public static final int KEY_ESCAPE = GLFW_KEY_ESCAPE;
	public static final int KEY_F1 = GLFW_KEY_F1;
	public static final int KEY_F2 = GLFW_KEY_F2;
	public static final int KEY_F3 = GLFW_KEY_F3;
	public static final int KEY_F4 = GLFW_KEY_F4;
	public static final int KEY_F5 = GLFW_KEY_F5;
	public static final int KEY_F6 = GLFW_KEY_F6;
	public static final int KEY_F7 = GLFW_KEY_F7;
	public static final int KEY_F8 = GLFW_KEY_F8;
	public static final int KEY_F9 = GLFW_KEY_F9;
	public static final int KEY_F10 = GLFW_KEY_F10;
	public static final int KEY_F11 = GLFW_KEY_F11;
	public static final int KEY_F12 = GLFW_KEY_F12;
	public static final int KEY_GRAVE = GLFW_KEY_GRAVE_ACCENT;
	public static final int KEY_1 = GLFW_KEY_1;
	public static final int KEY_2 = GLFW_KEY_2;
	public static final int KEY_3 = GLFW_KEY_3;
	public static final int KEY_4 = GLFW_KEY_4;
	public static final int KEY_5 = GLFW_KEY_5;
	public static final int KEY_6 = GLFW_KEY_6;
	public static final int KEY_7 = GLFW_KEY_7;
	public static final int KEY_8 = GLFW_KEY_8;
	public static final int KEY_9 = GLFW_KEY_9;
	public static final int KEY_0 = GLFW_KEY_0;
	public static final int KEY_MINUS = GLFW_KEY_MINUS;
	public static final int KEY_EQUAL = GLFW_KEY_EQUAL;
	public static final int KEY_BACKSPACE = GLFW_KEY_BACKSPACE;
	public static final int KEY_A = GLFW_KEY_A;
	public static final int KEY_B = GLFW_KEY_B;
	public static final int KEY_C = GLFW_KEY_C;
	public static final int KEY_D = GLFW_KEY_D;
	public static final int KEY_E = GLFW_KEY_E;
	public static final int KEY_F = GLFW_KEY_F;
	public static final int KEY_G = GLFW_KEY_G;
	public static final int KEY_H = GLFW_KEY_H;
	public static final int KEY_I = GLFW_KEY_I;
	public static final int KEY_J = GLFW_KEY_J;
	public static final int KEY_K = GLFW_KEY_K;
	public static final int KEY_L = GLFW_KEY_L;
	public static final int KEY_M = GLFW_KEY_M;
	public static final int KEY_N = GLFW_KEY_N;
	public static final int KEY_O = GLFW_KEY_O;
	public static final int KEY_P = GLFW_KEY_P;
	public static final int KEY_Q = GLFW_KEY_Q;
	public static final int KEY_R = GLFW_KEY_R;
	public static final int KEY_S = GLFW_KEY_S;
	public static final int KEY_T = GLFW_KEY_T;
	public static final int KEY_U = GLFW_KEY_U;
	public static final int KEY_V = GLFW_KEY_V;
	public static final int KEY_W = GLFW_KEY_W;
	public static final int KEY_X = GLFW_KEY_X;
	public static final int KEY_Y = GLFW_KEY_Y;
	public static final int KEY_Z = GLFW_KEY_Z;
	public static final int KEY_SPACE = GLFW_KEY_SPACE;
	public static final int KEY_TAB = GLFW_KEY_TAB;
	public static final int KEY_CAPSLOCK = GLFW_KEY_CAPS_LOCK;
	public static final int KEY_LEFT_SHIFT = GLFW_KEY_LEFT_SHIFT;
	public static final int KEY_LEFT_CTRL = GLFW_KEY_LEFT_CONTROL;
	public static final int KEY_LEFT_ALT = GLFW_KEY_LEFT_ALT;
	public static final int KEY_RIGHT_CTRL = GLFW_KEY_RIGHT_CONTROL;
	public static final int KEY_RIGHT_SHIFT = GLFW_KEY_RIGHT_SHIFT;
	public static final int KEY_PERIOD = GLFW_KEY_PERIOD;
	public static final int KEY_COMMA = GLFW_KEY_COMMA;
	public static final int KEY_SLASH = GLFW_KEY_SLASH;
	public static final int KEY_SEMICOLON = GLFW_KEY_SEMICOLON;
	public static final int KEY_APOSTROPHE = GLFW_KEY_APOSTROPHE;
	public static final int KEY_LEFT_BRACKET = GLFW_KEY_LEFT_BRACKET;
	public static final int KEY_RIGHT_BRACKET = GLFW_KEY_RIGHT_BRACKET;
	public static final int KEY_BACKSLASH = GLFW_KEY_BACKSLASH;
	public static final int KEY_ENTER = GLFW_KEY_ENTER;
	public static final int KEY_LEFT = GLFW_KEY_LEFT;
	public static final int KEY_UP = GLFW_KEY_UP;
	public static final int KEY_RIGHT = GLFW_KEY_RIGHT;
	public static final int KEY_DOWN = GLFW_KEY_DOWN;
	public static final int KEY_HOME = GLFW_KEY_HOME;
	public static final int KEY_END = GLFW_KEY_END;
	public static final int KEY_INSERT = GLFW_KEY_INSERT;
	public static final int KEY_DEL = GLFW_KEY_DELETE;
	public static final int KEY_PAGE_UP = GLFW_KEY_PAGE_UP;
	public static final int KEY_PAGE_DOWN = GLFW_KEY_PAGE_DOWN;
	public static final int KEY_PRINT_SCR = GLFW_KEY_PRINT_SCREEN;
	public static final int KEY_SCROLL_LOCK = GLFW_KEY_SCROLL_LOCK;
	public static final int KEY_PAUSE = GLFW_KEY_PAUSE;

	private final Set<Integer> buttonsPressed = new HashSet<>();
	private final Set<Integer> buttonsLast = new HashSet<>();

	private final Vector2i mousePos = new Vector2i();

	private Vector2i scrollAmount = new Vector2i();

	private final Set<Integer> keysPressed = new HashSet<>();
	private final Set<Integer> keysLast = new HashSet<>();

	public void setKeyPressed(int key, boolean pressed) {
		if (pressed)
			keysPressed.add(key);
		else
			keysPressed.remove(key);
	}

	public boolean isKeyJustPressed(int key) {
		return keysPressed.contains(key) && !keysLast.contains(key);
	}

	public boolean isKeyPressed(int key) {
		return keysPressed.contains(key) || keysLast.contains(key);
	}

	public boolean isKeyJustReleased(int key) {
		return !keysPressed.contains(key) && keysLast.contains(key);
	}

	public void setButtonPressed(int button, boolean pressed) {
		if (pressed)
			buttonsPressed.add(button);
		else
			buttonsPressed.remove(button);
	}

	public boolean isButtonJustPressed(int button) {
		return buttonsPressed.contains(button) && !buttonsLast.contains(button);
	}

	public boolean isButtonPressed(int button) {
		return buttonsPressed.contains(button) || buttonsLast.contains(button);
	}

	public boolean isButtonJustReleased(int button) {
		return !buttonsPressed.contains(button) && buttonsLast.contains(button);
	}

	public void setMousePos(double x, double y) {
		mousePos.x = (int) x;
		mousePos.y = (int) y;
	}

	public Vector2i getMousePos() {
		return new Vector2i(mousePos);
	}

	public void setScrollAmount(double x, double y) {
		scrollAmount.x = (int) x;
		scrollAmount.y = (int) y;
	}

	public int getScrollAmount() {
		return scrollAmount.y();
	}

	public Vector2i getScrollAmount2D() {
		return scrollAmount;
	}

	public void update() {
		scrollAmount = new Vector2i();
		buttonsLast.clear();
		buttonsLast.addAll(buttonsPressed);
		keysLast.clear();
		keysLast.addAll(keysPressed);
	}
}
