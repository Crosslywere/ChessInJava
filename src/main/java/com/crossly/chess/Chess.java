package com.crossly.chess;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.Camera3D;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.IdFramebuffer;
import com.crossly.engine.graphics.Shader;
import com.crossly.engine.input.Input;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Chess extends Engine {

	private final ArrayList<ChessPiece> chessPieces = new ArrayList<>(32);
	private final Camera3D camera = new Camera3D(0.5f, 3.5f, -5f, 16f / 9f);
	private IdFramebuffer framebuffer;
	private int selectedIndex = -1;
	private Shader basicShader;

	public Chess() {
		super();
		setWindowResizable(false);
		setWindowWidth(1280);
		setWindowHeight(720);
	}

	@Override
	public void onCreate() {
		framebuffer = new IdFramebuffer(getWindowWidth(), getWindowHeight());
		basicShader = new Shader("shader/basic.vert", "shader/basic.frag");
		for (int i = 0; i < 8; i++) {
			chessPieces.add(new ChessPiece(i, ChessPiece.PAWN, new Vector2f(i - 3f, 0f), new Vector3f(1f, 0f, 0f)));
		}
		camera.setPitch(15f);
	}

	@Override
	public void onUpdate(Input input) {
		running = !input.isKeyPressed(Input.KEY_ESCAPE);
		var mousePos = input.getMousePos();
		if (input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
			int prevSelectedIndex = selectedIndex;
			selectedIndex = framebuffer.getId(mousePos.x(), getWindowHeight() - mousePos.y());
			// Color selected piece
			if (selectedIndex > -1 && selectedIndex < chessPieces.size()) {
				chessPieces.get(selectedIndex).setColor(new Vector3f(1f));
			}
			// Revert to original piece color
			if (prevSelectedIndex != selectedIndex && prevSelectedIndex > -1 && prevSelectedIndex < chessPieces.size()) {
				chessPieces.get(prevSelectedIndex).setColor(new Vector3f(1f, 0f, 0f));
			}
		}
	}

	@Override
	public void onRender() {
		framebuffer.bind();
		framebuffer.clear();
		{
			basicShader.use();
			basicShader.setMatrix4("u_ProjView", camera.getProjectionViewMatrix());
			basicShader.setFloat3("u_LightDir", new Vector3f(1f).normalize());
			chessPieces.forEach(cp -> cp.draw(basicShader));
		}
		Framebuffer.unbind();
		Framebuffer.clearScreen();
		framebuffer.drawToScreen();
	}

	@Override
	public void onExit() {
	}

	public static void main(String[] args) {
		new Chess().play();
	}
}
