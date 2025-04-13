package com.crossly.chess;

import com.crossly.engine.graphics.Camera3D;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.Mesh;
import com.crossly.engine.graphics.Shader;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoardManager {

	private static final Shader PIECE_DIFFUSE_SHADER, BOARD_FLAT_SHADER;
	private static final Vector4f BOARD_LIGHT, BOARD_DARK;

	static {
		PIECE_DIFFUSE_SHADER = new Shader(
				"""
						#version 330 core
						layout (location = 0) in vec3 aPos;
						layout (location = 2) in vec3 aNorm;
						uniform mat4 uProjView;
						uniform mat4 uModel;
						out vec3 iNorm;
						void main() {
							iNorm = mat3(transpose(inverse(uModel))) * aNorm;
							gl_Position = uProjView * uModel * vec4(aPos, 1.0);
						}
						""",
				"""
						#version 330 core
						#define LIGHT_DIR normalize(vec3(1.0, 1.0, -1.0))
						layout (location = 0) out vec4 oFragColor;
						layout (location = 1) out int oPieceId;
						uniform vec3 uColor;
						uniform int uPieceId;
						in vec3 iNorm;
						void main() {
							oPieceId = uPieceId;
							float diffuseMul = max(dot(LIGHT_DIR, iNorm), 0.0);
							oFragColor = vec4((0.25 + diffuseMul) * uColor, 1.0);
						}
						""",
				false
		);
		BOARD_FLAT_SHADER = new Shader(
				"""
						#version 330 core
						layout (location = 0) in vec2 aPos;
						uniform mat4 uProjView;
						uniform mat4 uModel;
						void main() {
							vec4 pos = vec4(aPos.x * 0.5, 0.0, aPos.y * 0.5, 1.0);
							gl_Position = uProjView * uModel * pos;
						}
						""",
				"""
						#version 330 core
						layout (location = 0) out vec4 oFragColor;
						layout (location = 2) out int oBoardId;
						uniform vec4 uColor;
						uniform int uBoardId;
						void main() {
							oBoardId = uBoardId;
							oFragColor = uColor;
						}
						""",
				false
		);
		BOARD_LIGHT = new Vector4f(.8f, .75f, .7f, 1);
		BOARD_DARK = new Vector4f(.22f, .18f, .1f, 1);
	}

	private interface MoveAction {
		void fn();
	}

	private BoardFramebuffer framebuffer;
	private final ArrayList<ChessPiece> pieces = new ArrayList<>(32);
	private final Camera3D camera;

	public BoardManager(int width, int height) {
		framebuffer = new BoardFramebuffer(width, height);
		for (int i = 1; i <= 16; i++) {
			if (i <= 8) {
				pieces.add(new ChessPiece(i, ChessPiece.Type.PAWN, ChessPiece.Color.WHITE, new Vector2f(i, 2)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.PAWN, ChessPiece.Color.BLACK, new Vector2f(i, 7)));
			} else if (i == 9 || i == 16) {
				pieces.add(new ChessPiece(i, ChessPiece.Type.ROOK, ChessPiece.Color.WHITE, new Vector2f(i - 8, 1)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.ROOK, ChessPiece.Color.BLACK, new Vector2f(i - 8, 8)));
			} else if (i == 10 || i == 15) {
				pieces.add(new ChessPiece(i, ChessPiece.Type.KNIGHT, ChessPiece.Color.WHITE, new Vector2f(i - 8, 1)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.KNIGHT, ChessPiece.Color.BLACK, new Vector2f(i - 8, 8)));
			} else if (i == 11 || i == 14) {
				pieces.add(new ChessPiece(i, ChessPiece.Type.BISHOP, ChessPiece.Color.WHITE, new Vector2f(i - 8, 1)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.BISHOP, ChessPiece.Color.BLACK, new Vector2f(i - 8, 8)));
			} else if (i == 12) {
				pieces.add(new ChessPiece(i, ChessPiece.Type.KING, ChessPiece.Color.WHITE, new Vector2f(i - 8, 1)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.KING, ChessPiece.Color.BLACK, new Vector2f(i - 8, 8)));
			} else {
				pieces.add(new ChessPiece(i, ChessPiece.Type.QUEEN, ChessPiece.Color.WHITE, new Vector2f(i - 8, 1)));
				pieces.add(new ChessPiece(i + 16, ChessPiece.Type.QUEEN, ChessPiece.Color.BLACK, new Vector2f(i - 8, 8)));
			}
		}
		camera = new Camera3D(4.5f, 8, -4, (float) width / height);
		camera.setFovY(33);
		camera.setPitch(45);
	}

	public void resizeFramebuffer(int width, int height) {
		framebuffer.delete();
		framebuffer = new BoardFramebuffer(width, height);
		camera.setAspect((float) width / height);
	}

	public void render() {
		framebuffer.bind();
		framebuffer.clear();
		{
			BOARD_FLAT_SHADER.use();
			BOARD_FLAT_SHADER.setMatrix4("uProjView", camera.getProjectionViewMatrix());
			for (int y = 1; y <= 8; y++) {
				for (int x = 1; x <= 8; x++) {
					BOARD_FLAT_SHADER.setMatrix4("uModel", new Matrix4f().translate(x, 0, y));
					BOARD_FLAT_SHADER.setFloat4("uColor", (x + y) % 2 == 1 ? BOARD_LIGHT : BOARD_DARK);
					BOARD_FLAT_SHADER.setInt("uBoardId", BoardFramebuffer.Data.generateBoardPosId(x, y));
					Mesh.UNIT_2D_MESH.draw();
				}
			}
			PIECE_DIFFUSE_SHADER.use();
			PIECE_DIFFUSE_SHADER.setMatrix4("uProjView", camera.getProjectionViewMatrix());
			for (var piece : pieces) {
				PIECE_DIFFUSE_SHADER.setMatrix4("uModel", new Matrix4f()
						.translate(piece.getPosition().x(), 0, piece.getPosition().y())
						.scale(.8f)
						.rotateY(piece.getType() == ChessPiece.Type.KNIGHT ? (float) Math.toRadians(piece.getColor() == ChessPiece.Color.BLACK ? 90 : -90) : 0));
				PIECE_DIFFUSE_SHADER.setFloat3("uColor", selectedPiece != null && selectedPiece.getPieceId() == piece.getPieceId() ?
						new Vector3f(.8f, .6f, 0) : piece.getColor().getColor());
				PIECE_DIFFUSE_SHADER.setInt("uPieceId", piece.getPieceId());
				piece.getType().getModel().draw(PIECE_DIFFUSE_SHADER);
			}
		}
		Framebuffer.unbind();
		framebuffer.drawToScreen();
	}

	// Picking here means that the selection
	// - generates moves
	// - picks pieces
	// - takes piece action if a piece is selected

	private ChessPiece selectedPiece = null;
	private boolean selected = false;
	private ChessPiece.Color turn = ChessPiece.Color.WHITE;
	private Map<Integer, MoveAction> moveActions = new HashMap<>(21);

	// 1. move
	// 2. take
	// 3. select
	public void pick(Vector2i screenPos) {
		var data = framebuffer.getIds(screenPos.x(), screenPos.y());
		if (selected && data.pieceId() >= 0) {
			var piece = pieces.stream().filter(p -> p.getPieceId() == data.pieceId() && p.isInPlay()).findFirst().orElse(null);
			if (piece == null) {
				selectedPiece = null;
			}

			// Take or deselect
		} else if (selected && data.boardPosId() >= 0) {
			// Move or deselect
		} else {
			selectedPiece = pieces.stream().filter(piece -> piece.getPieceId() == data.pieceId() && piece.getColor() == turn && piece.isInPlay()).findFirst().orElse(selectedPiece);
			if (selectedPiece == null) {
				selectedPiece = pieces.stream().filter(piece -> {
					int x = (int) piece.getPosition().x();
					int y = (int) piece.getPosition().y();
					return data.boardPosId() == BoardFramebuffer.Data.generateBoardPosId(x, y) && piece.getColor() == turn && piece.isInPlay();
				}).findFirst().orElse(null);
			}
			if (selectedPiece != null)
				generateMoves(selectedPiece);
		}
		selected = selectedPiece != null;
	}

	private ChessPiece getPieceAtPosition(int x, int y) {
		return null;
	}

	private ChessPiece getPieceAtBoardId(int boardId) {
		return null;
	}

	private void generateMoves(ChessPiece piece) {
		moveActions.clear();
		int dir = piece.getColor() == ChessPiece.Color.WHITE ? 1 : -1;
		switch (piece.getType()) {
			case PAWN -> {
				int x = (int) piece.getPosition().x();
				if (getPieceAtPosition(x, (int) piece.getPosition().y() + dir) == null) {
					moveActions.put(BoardFramebuffer.Data.generateBoardPosId(x, (int) piece.getPosition().y() + dir), () -> {
						piece.moveTo(new Vector2f(x, piece.getPosition().y() + dir));
						turn = piece.getColor() == ChessPiece.Color.WHITE ? ChessPiece.Color.BLACK : ChessPiece.Color.WHITE;
					});
				}
			}
		}
	}
}
