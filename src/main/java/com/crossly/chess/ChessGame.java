package com.crossly.chess;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.FontAtlas;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.TextWriter;
import com.crossly.engine.input.Input;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class ChessGame extends Engine {

	private BoardManager boardManager;
	private TextWriter writer;
	private boolean overlaid = true;

	private static final int INIT_WIDTH = 1280, INIT_HEIGHT = 720;

	public ChessGame() {
		setWindowTitle("3D Chess Game - Jude Ogboru");
		setWindowWidth(INIT_WIDTH);
		setWindowHeight(INIT_HEIGHT);
		setWindowResizable(true);
	}

	public void onCreate() {
		writer = new TextWriter(new FontAtlas("fonts/IBMPlexSerif.ttf", 128), INIT_WIDTH, INIT_HEIGHT);
		boardManager = new BoardManager(getWindowWidth(), getWindowHeight());
	}

	public void onUpdate(Input input) {
		if (input.isKeyJustPressed(Input.KEY_ESCAPE)) {
			if (overlaid)
				running = false;
			else
				overlaid = true;
		}
		if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
			if (overlaid)
				overlaid = false;
			else {
				Vector2i pos = input.getMousePos();
				pos.y = getWindowHeight() - pos.y();
				boardManager.pick(pos);
			}
		}
		if (input.isKeyJustPressed(Input.KEY_D))
			boardManager.setDrawDebug(!boardManager.isDrawDebug());

		if (boardManager.isSwitchingSides())
			boardManager.rotateToSide();
	}

	public void onRender() {
		Framebuffer.clearScreen();
		boardManager.render();
		if (overlaid)
			renderOverlay();
	}

	public void onExit() {
		writer.getFontAtlas().delete();
		ChessPiece.destroyModels();
	}

	public void onResize() {
		if (getWindowWidth() > 0 && getWindowHeight() > 0) {
			writer.setViewMatrix(getWindowWidth(), getWindowHeight());
			boardManager.resizeFramebuffer(getWindowWidth(), getWindowHeight());
		}
	}

	public static void main(String[] args) {
		new ChessGame().play();
	}

	private void renderOverlay() {
		writer.writeText("""
				A 3D Chess Game by Jude Ogboru
				Instructions:
				- Left click on a piece to select it.
				- Left click on the void space to deselect.
				- Press the [Esc] key to exit the application.
				
				Click anywhere to resume...
				""", new Vector2f(8, 48), 48, new Vector3f(0, .5f, 1));
	}

}
