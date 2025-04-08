package com.crossly.chess;

import com.crossly.engine.graphics.Mesh;
import com.crossly.engine.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessBoard {

	private final Map<Integer, ChessPiece> boardMap = new HashMap<>(64);

	private final static Shader BOARD_SHADER;

	static {
		BOARD_SHADER = new Shader(
				"""
						#version 330 core
						layout (location = 0) in vec2 aPos;
						uniform mat4 uProjView;
						uniform mat4 uModel;
						void main() {
							vec3 pos = vec3(aPos.x, 0.0, aPos.y);
							gl_Position = uProjView * uModel * vec4(pos * 0.5, 1.0);
						}
						""",
				"""
						#version 330 core
						layout (location = 0) out vec4 oFragColor;
						layout (location = 1) out int oID;
						uniform int uID;
						uniform vec3 uColor;
						void main() {
							oID = uID;
							oFragColor = vec4(uColor, 1.0);
						}
						""",
				false
		);
	}

	public ChessBoard(ArrayList<ChessPiece> chessPieces) {
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				int id = (x << 4) + y;
				ChessPiece posPiece = null;
				for (var cp : chessPieces) {
					var position = cp.getPosition();
					int posX = (int) position.x();
					int posY = (int) position.y();
					if (posX == x && posY == y) {
						posPiece = cp;
						break;
					}
				}
				boardMap.put(id, posPiece);
			}
		}
	}

	public void drawBoard(Matrix4f projView, Vector3f offset) {
		BOARD_SHADER.use();
		BOARD_SHADER.setMatrix4("uProjView", projView);
		Vector3f color;
		boolean t = false;
		for (int x = 0; x < 8; x++) {
			t = !t;
			color = t ? new Vector3f(.8f, .75f, .7f) : new Vector3f(.22f, .18f, .1f);
			for (int z = 0; z < 8; z++) {
				Matrix4f model = new Matrix4f().translate(x, 0f, z).translate(offset);
				BOARD_SHADER.setMatrix4("uModel", model);
				BOARD_SHADER.setFloat3("uColor", color);
				var cp = boardMap.get((x << 4) + z);
				if (cp != null)
					BOARD_SHADER.setInt("uID", cp.getId());
				else
					BOARD_SHADER.setInt("uID", -1);
				Mesh.UNIT_2D_MESH.draw();
				t = !t;
				color = t ? new Vector3f(.8f, .75f, .7f) : new Vector3f(.22f, .18f, .1f);
			}
		}
	}
}
