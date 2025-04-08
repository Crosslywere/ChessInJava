package com.crossly.chess;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.*;
import com.crossly.engine.input.Input;
import com.crossly.engine.time.Timer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Chess extends Engine {

	private final ArrayList<ChessPiece> chessPieces = new ArrayList<>(32);
	private final Camera3D camera = new Camera3D(3.5f, 8f, -5f, 16f / 9f);
	private IdFramebuffer framebuffer;
	private int selectedIndex = -1;
	private Shader basicShader;
	private TextWriter playFairDisplay;
	private TextWriter spaceMono;
	private boolean overlay = true;
	private float overlayTimer = 0f;
	private Vector2f startTextPos = new Vector2f(8f, 96f);
	private final static float HEADER = 128f;
	private final static float NORMAL = 64f;
	private ChessBoard chessBoard;

	public Chess() {
		super();
		setWindowResizable(true);
		setWindowWidth(1280);
		setWindowHeight(720);
	}

	@Override
	public void onCreate() {
		framebuffer = new IdFramebuffer(getWindowWidth(), getWindowHeight());
		basicShader = new Shader("shader/basic.vert", "shader/basic.frag");
		for (int i = 0; i < 16; i++) {
			if (i < 8) {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.PAWN, new Vector2f(i, 1), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.PAWN, new Vector2f(i, 6), ChessPiece.Side.BLACK));
			} else if (i == 8 || i == 15) {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.ROOK, new Vector2f(i - 8, 0), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.ROOK, new Vector2f(i - 8, 7), ChessPiece.Side.BLACK));
			} else if (i == 9 || i == 14) {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.KNIGHT, new Vector2f(i - 8, 0), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.KNIGHT, new Vector2f(i - 8, 7), ChessPiece.Side.BLACK));
			} else if (i == 10 || i == 13) {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.BISHOP, new Vector2f(i - 8, 0), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.BISHOP, new Vector2f(i - 8, 7), ChessPiece.Side.BLACK));
			} else if (i == 12) {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.QUEEN, new Vector2f(i - 8, 0), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.QUEEN, new Vector2f(i - 8, 7), ChessPiece.Side.BLACK));
			} else {
				chessPieces.add(new ChessPiece(i, ChessPiece.Type.KING, new Vector2f(i - 8, 0), ChessPiece.Side.WHITE));
				chessPieces.add(new ChessPiece(i + 16, ChessPiece.Type.KING, new Vector2f(i - 8, 7), ChessPiece.Side.BLACK));
			}
		}
		camera.setPitch(45f);
		camera.setFovY(35f);
		playFairDisplay = new TextWriter(new FontAtlas("fonts/PlayfairDisplay.ttf", 128f), getWindowWidth(), getWindowHeight());
		playFairDisplay.setLineHeight(.25f);
		spaceMono = new TextWriter(new FontAtlas("fonts/SpaceMono.ttf", 128f), getWindowWidth(), getWindowHeight());
		spaceMono.setLineHeight(.75f);
		chessBoard = new ChessBoard(chessPieces);
		Framebuffer.setClearColor(.8f, .4f, .1f);
	}

	@Override
	public void onUpdate(Input input) {
		running = !input.isKeyPressed(Input.KEY_ESCAPE);
		var mousePos = input.getMousePos();
		if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
			int prevSelectedIndex = selectedIndex;
			selectedIndex = framebuffer.getId(mousePos.x(), getWindowHeight() - mousePos.y());
			// Selecting a piece
			if (selectedIndex > -1) {
				chessPieces.stream().filter(chessPiece -> chessPiece.getId() == selectedIndex).findFirst().ifPresent(ChessPiece::highLight);
				overlay = false;
			}
			// Deselecting a piece
			if (prevSelectedIndex != selectedIndex && prevSelectedIndex > -1) {
				chessPieces.stream().filter(chessPiece -> chessPiece.getId() == prevSelectedIndex).findFirst().ifPresent(ChessPiece::deselect);
			}
		}
		// Toggling the overlay
		if (input.isKeyJustPressed(Input.KEY_SPACE)) {
			overlay = !overlay;
			if (overlay) {
				startTextPos = new Vector2f(8f, 96f);
				overlayTimer = 0f;
			}
		}
		// Scrolling the overlay after 10s
		if (overlay && overlayTimer >= 10f)
			startTextPos.y -= Timer.getDeltaTime() * 100;
		else if (overlay)
			overlayTimer += Timer.getDeltaTime();
		if (input.isKeyJustPressed(Input.KEY_S))
			switchSides();
	}

	@Override
	public void onRender() {
		framebuffer.bind();
		framebuffer.clear();
		{
			chessBoard.drawBoard(camera.getProjectionViewMatrix(), new Vector3f());
			basicShader.use();
			basicShader.setMatrix4("u_ProjView", camera.getProjectionViewMatrix());
			basicShader.setFloat3("u_LightDir", new Vector3f(1, 1, -1).normalize());
			chessPieces.forEach(cp -> cp.draw(basicShader));
			if (overlay) drawOverlay();
		}
		Framebuffer.unbind();
		Framebuffer.clearScreen();
		framebuffer.drawToScreen();
	}

	@Override
	public void onResize() {
		if (getWindowWidth() > 0 && getWindowHeight() > 0) {
			framebuffer.delete();
			camera.setAspect((float) getWindowWidth() / getWindowHeight());
			framebuffer = new IdFramebuffer(getWindowWidth(), getWindowHeight());
			playFairDisplay.setViewMatrix(getWindowWidth(), getWindowHeight());
			spaceMono.setViewMatrix(getWindowWidth(), getWindowHeight());
		}
	}

	@Override
	public void onExit() {
		ChessPiece.PAWN.delete();
		ChessPiece.ROOK.delete();
		ChessPiece.KNIGHT.delete();
		ChessPiece.BISHOP.delete();
		ChessPiece.QUEEN.delete();
		ChessPiece.KING.delete();
		playFairDisplay.getFontAtlas().delete();
		spaceMono.getFontAtlas().delete();
		framebuffer.delete();
		basicShader.delete();
	}

	public static void main(String[] args) {
		new Chess().play();
	}

	private ChessPiece.Side currentSide = ChessPiece.Side.WHITE;

	private void switchSides() {
		if (currentSide == ChessPiece.Side.WHITE) {
			camera.setPosition(new Vector3f(3.5f, 8f, 12f));
			currentSide = ChessPiece.Side.BLACK;
			camera.setYaw(180);
		} else if (currentSide == ChessPiece.Side.BLACK) {
			camera.setPosition(new Vector3f(3.5f, 8f, -5f));
			currentSide = ChessPiece.Side.WHITE;
			camera.setYaw(0);
		}
	}

	private void drawOverlay() {
		playFairDisplay.setLineHeight(.25f);
		var endPos = playFairDisplay.writeText("A 3D Chess Game\n", new Vector2f(startTextPos), HEADER, new Vector3f(0, .5f, 1));
		playFairDisplay.setLineHeight(.75f);
		endPos = playFairDisplay.writeText("By Crosslywere(Jude Ogboru)!\n", new Vector2f(8, endPos.y()), NORMAL, new Vector3f(1, 1, 0));
		endPos = playFairDisplay.writeText("Click on a piece to select it!\nClick anywhere to deselect the piece!\nPress ", new Vector2f(8, endPos.y()), 64);
		endPos = spaceMono.writeText("[Esc]", endPos, NORMAL);
		endPos = playFairDisplay.writeText(" to exit!\n", endPos, NORMAL);
		endPos = playFairDisplay.writeText("Press ", new Vector2f(8, endPos.y()), NORMAL);
		endPos = spaceMono.writeText("[Space Bar]", endPos, NORMAL);
		endPos = playFairDisplay.writeText(" to toggle this overlay!", endPos, NORMAL);
		if (endPos.y() < 0) startTextPos = new Vector2f(8, getWindowHeight() + HEADER);
	}
}
