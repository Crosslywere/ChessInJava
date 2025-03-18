package com.crossly.engine;

import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;
import com.crossly.engine.window.Window;

public abstract class Engine {

	private int windowWidth;
	private int windowHeight;
	private String windowTitle;
	private boolean windowResizable;

	protected boolean running = true;

	protected Engine() {
		windowWidth = 800;
		windowHeight = 600;
		windowTitle = "Chess In Java";
		windowResizable = false;
	}

	protected abstract void onCreate();

	protected abstract void onUpdate(Input input);

	protected abstract void onRender();

	protected void onExit() {}

	public void onResize() {}

	public void play() {
		Input input;
		Window window = new Window(this, input = new Input());
		Timer.init();
		onCreate();
		while (window.isOpen() && running) {
			onUpdate(input);
			onRender();
			input.update();
			Timer.update();
		}
		onExit();
		window.cleanup();
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public boolean getWindowResizable() {
		return windowResizable;
	}

	public void setWindowResizable(boolean windowResizable) {
		this.windowResizable = windowResizable;
	}
}
