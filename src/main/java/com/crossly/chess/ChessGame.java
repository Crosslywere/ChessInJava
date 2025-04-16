package com.crossly.chess;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.FontAtlas;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.TextWriter;
import com.crossly.engine.input.Input;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.FileWriter;
import java.io.IOException;

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
		if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT) && !boardManager.isPiecePromotable()) {
			if (overlaid)
				overlaid = false;
			else {
				Vector2i pos = input.getMousePos();
				pos.y = getWindowHeight() - pos.y();
				boardManager.pick(pos);
			}
		} else if (boardManager.isPiecePromotable()) {
			if (input.isKeyPressed(Input.KEY_B)) {
				boardManager.promotePiece(ChessPiece.Type.BISHOP);
			}
			if (input.isKeyPressed(Input.KEY_Q)) {
				boardManager.promotePiece(ChessPiece.Type.QUEEN);
			}
			if (input.isKeyPressed(Input.KEY_R)) {
				boardManager.promotePiece(ChessPiece.Type.ROOK);
			}
			if (input.isKeyPressed(Input.KEY_K)) {
				boardManager.promotePiece(ChessPiece.Type.KNIGHT);
			}
		}
		if (input.isKeyJustPressed(Input.KEY_D))
			boardManager.setDrawDebug(!boardManager.isDrawDebug());

		if (input.isKeyJustPressed(Input.KEY_F5))
			quickSave();

		if (input.isKeyJustPressed(Input.KEY_F9))
			quickLoad();

		if (boardManager.isSwitchingSides())
			boardManager.rotateToSide();
	}

	public void onRender() {
		Framebuffer.clearScreen();
		boardManager.render();
		if (overlaid)
			renderOverlay();
		else if (boardManager.isPiecePromotable())
			renderPromotionOverlay();
		if (boardManager.isChecked())
			renderCheckOverlay();
	}

	public void onExit() {
		writer.getFontAtlas().delete();
		ChessPiece.destroyModels();
		boardManager.deleteFramebuffer();
		BoardManager.delete();
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

	private static final Vector3f FONT_RENDER_COLOR = new Vector3f(0, .5f, 1);

	private void renderCheckOverlay() {
		writer.writeText("Check...", new Vector2f(8, 48), 48, FONT_RENDER_COLOR);
	}

	private void renderPromotionOverlay() {
		writer.writeText("""
				A pawn can be promoted!
				Press on of the following keys to promote it!
				- [Q] for Queen
				- [B] for Bishop
				- [K] for Knight
				- [R] for Rook
				""", new Vector2f(8, 48), 48, FONT_RENDER_COLOR);
	}

	private void renderOverlay() {
		writer.writeText("""
				A 3D Chess Game by Jude Ogboru
				Instructions:
				- Left click on a piece to select it.
				- Left click on the void space to deselect.
				- Press the [D] key to toggle rendering possible moves.
				- Press the [Esc] key to exit the application.
				- [F5] Quick Save.
				- [F9] Quick Load.
				
				Click anywhere to resume...
				""", new Vector2f(8, 48), 48, FONT_RENDER_COLOR);
	}

	private void quickSave() {
		try {
			FileWriter writer = new FileWriter("save.txt");
			writer.write(boardManager.generateSave());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void quickLoad() {
		try {
			String filepath = Engine.getAbsolutePath("save.txt");
			boardManager.deleteFramebuffer();
			boardManager = new BoardManager(getWindowWidth(), getWindowHeight(), filepath);
		} catch (RuntimeException e) {
			System.err.println("No save file found!");
		}
	}
}
