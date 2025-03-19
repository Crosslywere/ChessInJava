package com.crossly.engine;

import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;
import com.crossly.engine.window.Window;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;

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

	// Static utility functions
	public static String getAbsolutePath(String path) {
		var url = Engine.class.getClassLoader().getResource(path);
		if (url != null)
			return URLDecoder.decode(url.getFile().substring(1), Charset.defaultCharset());
		File file = new File(path);
		if (!file.isFile())
			throw new RuntimeException("File '" + path + "' does not exist!");
		return file.getAbsolutePath();
	}
}
