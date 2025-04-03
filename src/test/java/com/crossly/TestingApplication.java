package com.crossly;

import com.crossly.engine.Engine;
import com.crossly.engine.audio.AudioSource;
import com.crossly.engine.graphics.*;
import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class TestingApplication extends Engine {

	// Variables
	private float time = 0f;
	private int frames = 0;
	final float INTERVAL = 0.5f;
	private Vector2i mousePosLast = new Vector2i();

	// OpenGL Dependent Graphics
	private Shader shader;
	private Model chessPiece;
	private IdFramebuffer idFramebuffer;
	private ImageTexture image;

	// Audio
	private AudioSource audioSource;

	// OpenGL Independent Graphics
	private final Camera3D camera;

	private FontAtlas robotoFontAtlas;

	public TestingApplication() {
		super();
		setWindowWidth(1280);
		setWindowHeight(720);
		setWindowTitle("Test Application");
		setWindowResizable(true);
		camera = new Camera3D(0f, 0f, -1f, (float) getWindowWidth() / getWindowHeight());
	}

	@Override
	public void onCreate() {
		shader = new Shader("test_simple.vert", "test_simple.frag");
		idFramebuffer = new IdFramebuffer(getWindowWidth(), getWindowHeight());
		audioSource = new AudioSource("stab-f-01-brvhrtz-224599.mp3", AudioSource.Format.MP3, .1f);
		image = new ImageTexture("wall.jpg", false, true);
		chessPiece = new Model("ChessPiece/ChessPiece.obj");
		robotoFontAtlas = new FontAtlas("sui.ttf", 128f);
		robotoFontAtlas.writeToImage("Test-FontAtlas.png");
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
		if (idFramebuffer.getId(x, y) == 1) {
			shader.setFloat("uTime", Timer.getTotalTime());
			if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
				audioSource.play();
			}
			shader.setFloat("uDelta", audioSource.getElapsedTime());
		} else {
			shader.setFloat("uTime", 0f);
			shader.setFloat("uDelta", 0f);
		}

		if (input.isKeyPressed(Input.KEY_ESCAPE))
			running = false;

		int gain = input.getScrollAmount();
		if (gain != 0) {
			audioSource.setVolume(Math.clamp(audioSource.getVolume() + (gain * .05f), 0f, 1f));
			System.out.println("Audio Volume - " + audioSource.getVolume());
		}

		if (input.isKeyPressed(Input.KEY_W))
			camera.addPosition(camera.getFront().mul(Timer.getDeltaTime()));
		if (input.isKeyPressed(Input.KEY_S))
			camera.addPosition(camera.getFront().mul(-Timer.getDeltaTime()));
		if (input.isKeyPressed(Input.KEY_A))
			camera.addPosition(camera.getRight().mul(-Timer.getDeltaTime()));
		if (input.isKeyPressed(Input.KEY_D))
			camera.addPosition(camera.getRight().mul(Timer.getDeltaTime()));

		Vector2i mousePosDiff = new Vector2i();
		mousePos.sub(mousePosLast, mousePosDiff);
		mousePosLast = mousePos;
		if (input.isButtonPressed(Input.MOUSE_BUTTON_RIGHT)) {
			input.disableMouse();
			camera.rotateBy(new Vector2f(mousePosDiff.x * -Timer.getDeltaTime() * 3f, mousePosDiff.y * Timer.getDeltaTime() * 3f));
		}
		if (input.isButtonJustReleased(Input.MOUSE_BUTTON_RIGHT))
			input.enableMouse();
	}

	@Override
	public void onRender() {
		idFramebuffer.bind();
		{   idFramebuffer.clear();
			shader.use();
			shader.setInt("uID", 1);
			image.bind(0);
			shader.setMatrix4("uProjView", camera.getProjectionViewMatrix());
			shader.setMatrix4("uModel", new Matrix4f());
			chessPiece.draw(shader);
		}
		Framebuffer.unbind();
		Framebuffer.clearScreen();
		idFramebuffer.drawToScreen();
		frames++;
	}

	@Override
	public void onExit() {
		super.onExit();
		shader.delete();
		idFramebuffer.delete();
		audioSource.delete();
		chessPiece.delete();
		robotoFontAtlas.delete();
	}

	@Override
	public void onResize() {
		super.onResize();
		idFramebuffer.delete();
		idFramebuffer = new IdFramebuffer(getWindowWidth(), getWindowHeight());
		camera.setAspect((float) getWindowWidth() / getWindowHeight());
	}

	public static void main(String[] args) {
		new TestingApplication().play();
	}
}
