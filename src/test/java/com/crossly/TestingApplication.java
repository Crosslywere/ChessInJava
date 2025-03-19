package com.crossly;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.Shader;
import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;

public class TestingApplication extends Engine {

	private float time = 0f;
	private int frames = 0;
	final float INTERVAL = 0.5f;
	private Shader shader;

	public TestingApplication() {
		super();
		setWindowWidth(1200);
		setWindowHeight(800);
		setWindowTitle("Test Application");
		setWindowResizable(true);
	}

	@Override
	public void onCreate() {
		shader = new Shader("""
				#version 330 core
				layout (location = 0) in vec3 aPos;
				void main() {
					gl_Position = vec4(aPos, 1.0);
				}""",
				"""
				#version 330 core
				void main() {
					gl_FragColor = vec4(1.0);
				}""",
				false);
	}

	@Override
	public void onUpdate(Input input) {
		time += Timer.getDeltaTime();
		if (time >= INTERVAL) {
			System.out.printf("FPS: %d, FrameTime: %.2fms\n", (int) (frames / INTERVAL), (time / frames) * 1_000);
			time -= INTERVAL;
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
		shader.delete();
	}

	@Override
	public void onResize() {
		super.onResize();
	}

	public static void main(String[] args) {
		new TestingApplication().play();
	}
}
