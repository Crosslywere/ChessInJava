package com.crossly;

import com.crossly.engine.Engine;
import com.crossly.engine.audio.AudioSource;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.Mesh;
import com.crossly.engine.graphics.Shader;
import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;

public class TestingApplication extends Engine {

	private float time = 0f;
	private int frames = 0;
	final float INTERVAL = 0.5f;
	private Shader shader;
	private Mesh mesh;
	private Framebuffer screenFramebuffer;

	private AudioSource audioSource;

	public TestingApplication() {
		super();
		setWindowWidth(1200);
		setWindowHeight(800);
		setWindowTitle("Test Application");
		setWindowResizable(true);
	}

	@Override
	public void onCreate() {
		shader = new Shader("test_simple.vert", "test_simple.frag");
		mesh = new Mesh(
				new float[]{
						0.0f, 0.5f,
						0.5f,-0.5f,
						-.5f,-0.5f
				},
				null,
				new float[]{
						1f, 0f, 0f,
						0f, 1f, 0f,
						0f, 0f, 1f,
				},
				new int[]{
						0, 1, 2
				},
				false);
		screenFramebuffer = new Framebuffer(getWindowWidth(), getWindowHeight());
		audioSource = new AudioSource("stab-f-01-brvhrtz-224599.mp3", AudioSource.Format.MP3);
	}

	@Override
	public void onUpdate(Input input) {
		time += Timer.getDeltaTime();
		if (time >= INTERVAL) {
			System.out.printf("FPS: %d, FrameTime: %.2fms\n", (int) (frames / INTERVAL), (time / frames) * 1_000);
			time -= INTERVAL;
			frames = 0;
		}

		var mousePos = input.getMousePos();
		int x = mousePos.x(), y = getWindowHeight() - mousePos.y();
		shader.use();
		if (screenFramebuffer.getId(x, y) == 1) {
			shader.setFloat("uTime", Timer.getTotalTime());
			if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
				audioSource.play();
			}
			shader.setFloat("uDelta", audioSource.getElapsedTime());
		} else {
			shader.setFloat("uTime", 0f);
		}

		if (input.isKeyPressed(Input.KEY_ESCAPE))
			running = false;

		int gain = input.getScrollAmount();
		if (gain != 0) {
			audioSource.setVolume(Math.clamp(audioSource.getVolume() + (gain * .05f), 0f, 1f));
			System.out.println("Audio Volume - " + audioSource.getVolume());
		}
	}

	@Override
	public void onRender() {
		Framebuffer.drawTo(screenFramebuffer);
		{	Framebuffer.clear();
			shader.use();
			shader.setInt("uID", 1);
			mesh.draw();
		}
		Framebuffer.unbind();
		Framebuffer.renderToScreen(screenFramebuffer);
		frames++;
	}

	@Override
	public void onExit() {
		super.onExit();
		shader.delete();
		mesh.delete();
		screenFramebuffer.delete();
		audioSource.delete();
	}

	@Override
	public void onResize() {
		super.onResize();
	}

	public static void main(String[] args) {
		new TestingApplication().play();
	}
}
