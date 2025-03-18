package com.crossly;

import com.crossly.engine.Engine;
import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;

public class TestingApplication extends Engine {

	private float time = 0f;
	private int frames = 0;

	public TestingApplication() {
		setWindowWidth(1200);
		setWindowHeight(800);
		setWindowTitle("Test Application");
		setWindowResizable(true);
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onUpdate(Input input) {
		time += Timer.getDeltaTime();
		if (time >= 1f) {
			time -= 1f;
			System.out.println("FPS:" + frames);
			frames = 0;
		}
		if (input.isKeyPressed(Input.KEY_ESCAPE))
			running = false;
	}

	@Override
	public void onRender() {
		frames++;
	}

	@Override
	public void onExit() {
		super.onExit();
	}

	@Override
	public void onResize() {
		super.onResize();
	}

	public static void main(String[] args) {
		new TestingApplication().play();
	}
}
