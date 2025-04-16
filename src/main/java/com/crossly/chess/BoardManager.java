package com.crossly.chess;

import com.crossly.engine.Engine;
import com.crossly.engine.graphics.Camera3D;
import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.Mesh;
import com.crossly.engine.graphics.Shader;
import com.crossly.engine.time.Timer;
import org.joml.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
	private ChessPiece selectedPiece = null;
	private boolean selected = false;
	private ChessPiece.Color turn = ChessPiece.Color.WHITE;
	private final Map<Integer, MoveAction> moveActions = new HashMap<>(28);
	private final Vector2f outPositionWhite = new Vector2f(0, 1);
	private final Vector2f outPositionBlack = new Vector2f(9, 8);
	private boolean switchingSides = false;
	private float timer = 0;
	private boolean drawDebug = false;
	private int promotablePieceId = -1;
	private ChessPiece checkingPiece = null;

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

	public BoardManager(int width, int height, String filepath) {
		this(width, height);
		try {
			String data = new String(Files.readAllBytes(Paths.get(Engine.getAbsolutePath(filepath))));
			pieces.clear();
			Scanner scn = new Scanner(data);
			ChessPiece.Color current = null;
			while (scn.hasNextLine()) {
				String line = scn.nextLine();
				if (line.contains("#WHITE")) {
					current = ChessPiece.Color.WHITE;
					continue;
				}else if (line.contains("#BLACK")) {
					current = ChessPiece.Color.BLACK;
					continue;
				} else if (line.contains("#EXTRA") && scn.hasNextLine()) {
					current = null;
					continue;
				}
				if (current != null) {
					var piece = new ChessPiece(line, current);
					var position = piece.getPosition();
					if (position.x() > 8 || position.x() <= 0) {
						piece.setInPlay(false);
						getOutPosition(current == ChessPiece.Color.BLACK ? ChessPiece.Color.WHITE : ChessPiece.Color.BLACK);
					}
					pieces.add(piece);
				} else {
					turn = ChessPiece.Color.valueOf(line);
					if (turn == ChessPiece.Color.BLACK) {
						camera.setYaw(180);
						camera.setPosition(new Vector3f(4.5f, 8, 13));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
					if (moveActions.containsKey(BoardFramebuffer.Data.generateBoardPosId(x, y)) && selectedPiece != null && drawDebug)
						BOARD_FLAT_SHADER.setFloat4("uColor", selectedPiece.getColor() == ChessPiece.Color.WHITE ? new Vector4f(0, .3f, .6f, 1) : new Vector4f(.8f, .2f, .1f, 1));
					else
						BOARD_FLAT_SHADER.setFloat4("uColor", (x + y) % 2 == 1 ? BOARD_DARK : BOARD_LIGHT);
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

	public boolean isSwitchingSides() {
		return switchingSides;
	}

	public boolean isDrawDebug() {
		return drawDebug;
	}

	public boolean isPiecePromotable() {
		return promotablePieceId > 0;
	}

	public void promotePiece(ChessPiece.Type type) {
		pieces.stream().filter(p -> p.getPieceId() == promotablePieceId && p.isInPlay()).findFirst()
				.ifPresent(piece -> {
					piece.setType(type);
					swapSides(piece.getColor());
				});

		promotablePieceId = -1;
	}

	public void setDrawDebug(boolean drawDebug) {
		this.drawDebug = drawDebug;
	}

	public void rotateToSide() {
		timer += Timer.getDeltaTime();
		if (timer > .5f) {
			if (turn == ChessPiece.Color.WHITE) {
				camera.setPosition(camera.getPosition().lerp(new Vector3f(4.5f, 8, -4), timer - .5f));
				camera.setYaw(180 + (timer - .5f) * 180);
			} else {
				camera.setPosition(camera.getPosition().lerp(new Vector3f(4.5f, 8, 13), timer - .5f));
				camera.setYaw((timer - .5f) * 180);
			}
			if (timer - .5f >= 1) {
				switchingSides = false;
				timer = 0;
				camera.setPosition(turn == ChessPiece.Color.WHITE ? new Vector3f(4.5f, 8, -4) : new Vector3f(4.5f, 8, 13));
				camera.setYaw(turn == ChessPiece.Color.WHITE ? 0 : 180);
			}
		}
	}

	// Order: Take > Move > Selection
	public void pick(Vector2i screenPos) {
		var data = framebuffer.getIds(screenPos.x(), screenPos.y());
		if (selected && data.pieceId() >= 0) {
			pieces.stream().filter(p -> p.getPieceId() == data.pieceId() && p.isInPlay()).findFirst()
					.ifPresentOrElse(piece -> {
						int boardId = BoardFramebuffer.Data.generateBoardPosId(piece.getPosition());
						if (moveActions.containsKey(boardId))
							moveActions.get(boardId).fn();
					}, () -> selectedPiece = null);
			// Take or deselect
		} else if (selected && data.boardPosId() >= 0) {
			// Move or deselect
			if (moveActions.containsKey(data.boardPosId())) {
				moveActions.get(data.boardPosId()).fn();
				if (selectedPiece != null && selectedPiece.getType() == ChessPiece.Type.PAWN && ((int) selectedPiece.getPosition().y() == 8 || (int) selectedPiece.getPosition().y() == 1)) {
					promotablePieceId = selectedPiece.getPieceId();
				}
			} else
				selectedPiece = null;
		} else {
			selectedPiece = pieces.stream().filter(piece -> piece.getPieceId() == data.pieceId() && piece.getColor() == turn && piece.isInPlay()).findFirst().orElse(null);
			if (selectedPiece == null) {
				selectedPiece = pieces.stream().filter(piece -> {
					int x = (int) piece.getPosition().x();
					int y = (int) piece.getPosition().y();
					return data.boardPosId() == BoardFramebuffer.Data.generateBoardPosId(x, y) && piece.getColor() == turn && piece.isInPlay();
				}).findFirst().orElse(null);
			}
			if (selectedPiece != null) {
				generateMoves(selectedPiece);
				if (checkingPiece != null) {
					cullUnsafe();
				}
			}
		}
		selected = selectedPiece != null;
	}

	public void deleteFramebuffer() {
		framebuffer.delete();
	}

	public static void delete() {
		BOARD_FLAT_SHADER.delete();
		PIECE_DIFFUSE_SHADER.delete();
	}

	public String generateSave() {
		StringBuilder saveData = new StringBuilder();
		saveData.append("#WHITE\n");
		for (var piece : pieces.stream().filter(cp -> ChessPiece.Color.WHITE == cp.getColor()).toList()) {
			saveData.append(piece.toString()).append('\n');
		}
		saveData.append("#BLACK\n");
		for (var piece : pieces.stream().filter(cp -> ChessPiece.Color.BLACK == cp.getColor()).toList()) {
			saveData.append(piece.toString()).append('\n');
		}
		saveData.append("#EXTRA\n");
		saveData.append(turn.name()).append('\n');
		return saveData.toString();
	}

	public boolean isChecked() {
		return checkingPiece != null;
	}

	private ChessPiece getPieceAtPosition(int x, int y) {
		return pieces.stream().filter(piece -> {
			int ppx = (int) piece.getPosition().x();
			int ppy = (int) piece.getPosition().y();
			return ppx == x && ppy == y && piece.isInPlay();
		}).findFirst().orElse(null);
	}

	private void swapSides(ChessPiece.Color color) {
		if (selectedPiece != null) {
			var takeMoves =  generateTakeMoves(selectedPiece);
			var king = pieces.stream().filter(piece -> piece.getType() == ChessPiece.Type.KING && piece.getColor() != color).findFirst().orElse(null);
			if (king != null) {
				int kp = BoardFramebuffer.Data.generateBoardPosId(king.getPosition());
				checkingPiece = takeMoves.contains(kp) ? selectedPiece : null;
			}
		}
		selectedPiece = null;
		turn = color == ChessPiece.Color.WHITE ? ChessPiece.Color.BLACK : ChessPiece.Color.WHITE;
		switchingSides = true;
	}

	private Vector2f getOutPosition(ChessPiece.Color color) {
		Vector2f position;
		if (color == ChessPiece.Color.WHITE) {
			position = new Vector2f(outPositionWhite);
			outPositionWhite.y += 1;
			if (outPositionWhite.y() > 8) {
				outPositionWhite.y = 1;
				outPositionWhite.x -= 1;
			}
		} else {
			position = new Vector2f(outPositionBlack);
			outPositionBlack.y -= 1;
			if (outPositionBlack.y() <= 0) {
				outPositionBlack.y = 8;
				outPositionBlack.x += 1;
			}
		}
		return position;
	}

	private void generateMoves(ChessPiece piece) {
		moveActions.clear();
		int dir = piece.getColor() == ChessPiece.Color.WHITE ? 1 : -1;
		int ppx = (int) piece.getPosition().x();
		int ppy = (int) piece.getPosition().y();
		switch (piece.getType()) {
			case PAWN -> {
				if (getPieceAtPosition(ppx, ppy + dir) == null) {
					moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + dir), () -> {
						piece.moveTo(new Vector2f(ppx, ppy + dir));
						if (ppy + dir < 8 && ppy + dir > 1) {
							swapSides(piece.getColor());
						}
					});
					if (piece.isNotMoved() && getPieceAtPosition(ppx, ppy + dir * 2) == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + dir * 2), () -> {
							piece.moveTo(new Vector2f(ppx, piece.getPosition().y() + dir * 2));
							swapSides(piece.getColor());
						});
					}
				}
				if (ppx + 1 <= 8) {
					var enemy = getPieceAtPosition(ppx + 1, (int) piece.getPosition().y() + dir);
					if (enemy != null && enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx + 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							if (ppy + dir < 8 && ppy + dir > 1) {
								swapSides(piece.getColor());
							}
						});
					}
				}
				if (ppx - 1 > 0) {
					var enemy = getPieceAtPosition(ppx - 1, (int) piece.getPosition().y() + dir);
					if (enemy != null && enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx - 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							if (ppy + dir < 8 && ppy + dir > 1) {
								swapSides(piece.getColor());
							}
						});
					}
				}
				// En passant
				if (piece.getColor() == ChessPiece.Color.WHITE) {
					if (ppy == 5 && getPieceAtPosition(ppx + 1, ppy) != null) {
						var enemy = getPieceAtPosition(ppx + 1, ppy);
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx + 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
					if (ppy == 5 && getPieceAtPosition(ppx - 1, ppy) != null) {
						var enemy = getPieceAtPosition(ppx - 1, ppy);
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx - 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				} else {
					if (ppy == 4 && getPieceAtPosition(ppx + 1, ppy) != null) {
						var enemy = getPieceAtPosition(ppx + 1, ppy);
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx + 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
					if (ppy == 4 && getPieceAtPosition(ppx - 1, ppy) != null) {
						var enemy = getPieceAtPosition(ppx - 1, ppy);
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + dir), () -> {
							piece.moveTo(new Vector2f(ppx - 1, ppy + dir));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
			}
			case ROOK -> {
				// To the right
				for (int x = 1; x <= 7; x++) {
					int offX = ppx + x;
					if (offX > 8) break;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the left
				for (int x = 1; x <= 7; x++) {
					int offX = ppx - x;
					if (offX <= 0) break;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()){
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the top
				for (int y = 1; y <= 7; y++) {
					int offY = ppy + y;
					if (offY > 8) break;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the bottom
				for (int y = 1; y <= 7; y++) {
					int offY = ppy - y;
					if (offY <= 0) break;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
			}
			case KNIGHT -> {
				int offX, offY;
				if ((offX = ppx + 2) <= 8 && (offY = ppy + 1) <= 8) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx + 2) <= 8 && (offY = ppy - 1) > 0) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 2) > 0 && (offY = ppy + 1) <= 8) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 2) > 0 && (offY = ppy - 1) > 0) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx + 1) <= 8 && (offY = ppy + 2) <= 8) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx + 1) <= 8 && (offY = ppy - 2) > 0) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 1) > 0 && (offY = ppy + 2) <= 8) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 1) > 0 && (offY = ppy - 2) > 0) {
					int x = offX, y = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(x, y));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
			}
			case BISHOP -> {
				// Up, Right
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx + offDir) <= 8 && (offY = ppy + offDir) <= 8) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Up, Left
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx - offDir) > 0 && (offY = ppy + offDir) <= 8) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Down, Right
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx + offDir) <= 8 && (offY = ppy - offDir) > 0) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Down, Left
				for (int offDir = 1; offDir <= 7; offDir++) {	int offX, offY;
					if ((offX = ppx - offDir) > 0 && (offY = ppy - offDir) > 0) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
			}
			case QUEEN -> {
				// To the right
				for (int x = 1; x <= 7; x++) {
					int offX = ppx + x;
					if (offX > 8) break;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the left
				for (int x = 1; x <= 7; x++) {
					int offX = ppx - x;
					if (offX <= 0) break;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()){
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(offX, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the top
				for (int y = 1; y <= 7; y++) {
					int offY = ppy + y;
					if (offY > 8) break;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// To the bottom
				for (int y = 1; y <= 7; y++) {
					int offY = ppy - y;
					if (offY <= 0) break;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor()) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, offY));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
						break;
					} else {
						break;
					}
				}
				// Up, Right
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx + offDir) <= 8 && (offY = ppy + offDir) <= 8) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Up, Left
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx - offDir) > 0 && (offY = ppy + offDir) <= 8) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Down, Right
				for (int offDir = 1; offDir <= 7; offDir++) {
					int offX, offY;
					if ((offX = ppx + offDir) <= 8 && (offY = ppy - offDir) > 0) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}
				}
				// Down, Left
				for (int offDir = 1; offDir <= 7; offDir++) {	int offX, offY;
					if ((offX = ppx - offDir) > 0 && (offY = ppy - offDir) > 0) {
						var enemy = getPieceAtPosition(offX, offY);
						if (enemy == null) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								swapSides(piece.getColor());
							});
						} else if (enemy.getColor() != piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
								piece.moveTo(new Vector2f(offX, offY));
								enemy.setPosition(getOutPosition(piece.getColor()));
								enemy.setInPlay(false);
								swapSides(piece.getColor());
							});
							break;
						} else {
							break;
						}
					}

				}
			}
			case KING -> {
				int offX, offY;
				if ((offX = ppx + 1) <= 8) {
					int fx = offX;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(fx, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(fx, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx + 1) <= 8 && (offY = ppy + 1) <= 8) {
					int fx = offX, fy = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offY = ppy + 1) <= 8) {
					int fy = offY;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, fy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 1) > 0 && (offY = ppy + 1) <= 8) {
					int fx = offX, fy = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 1) > 0) {
					int fx = offX;
					var enemy = getPieceAtPosition(offX, ppy);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(fx, ppy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, ppy), () -> {
							piece.moveTo(new Vector2f(fx, ppy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx - 1) > 0 && (offY = ppy - 1) > 0) {
					int fx = offX, fy = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offY = ppy - 1) > 0) {
					int fy = offY;
					var enemy = getPieceAtPosition(ppx, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
						piece.moveTo(new Vector2f(ppx, fy));
						swapSides(piece.getColor());
					});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx, offY), () -> {
							piece.moveTo(new Vector2f(ppx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				if ((offX = ppx + 1) <= 8 && (offY = ppy - 1) > 0) {
					int fx = offX, fy = offY;
					var enemy = getPieceAtPosition(offX, offY);
					if (enemy == null) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							swapSides(piece.getColor());
						});
					} else if (enemy.getColor() != piece.getColor() && enemy.getType() != ChessPiece.Type.KING) {
						moveActions.put(BoardFramebuffer.Data.generateBoardPosId(offX, offY), () -> {
							piece.moveTo(new Vector2f(fx, fy));
							enemy.setPosition(getOutPosition(piece.getColor()));
							enemy.setInPlay(false);
							swapSides(piece.getColor());
						});
					}
				}
				// King side castling
				if (piece.isNotMoved()) {
					boolean castlable = false;
					for (int i = 1; i < 3; i++) {
						castlable = getPieceAtPosition(ppx - i, ppy) == null;
						if (!castlable)
							break;
					}
					if (castlable) {
						var castle = getPieceAtPosition(ppx - 3, ppy);
						if (castle != null && castle.isNotMoved() && castle.getType() == ChessPiece.Type.ROOK && castle.getColor() == piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx - 2, ppy), () -> {
								piece.moveTo(new Vector2f(ppx - 2, ppy));
								castle.moveTo(new Vector2f(ppx - 1, ppy));
								swapSides(piece.getColor());
							});
						}
					}
				}
				// Queen side castling
				if (piece.isNotMoved()) {
					boolean castlable = false;
					for (int i = 1; i < 4; i++) {
						castlable = getPieceAtPosition(ppx + i, ppy) == null;
						if (!castlable)
							break;
					}
					if (castlable) {
						var castle = getPieceAtPosition(ppx + 4, ppy);
						if (castle != null && castle.isNotMoved() && castle.getType() == ChessPiece.Type.ROOK && castle.getColor() == piece.getColor()) {
							moveActions.put(BoardFramebuffer.Data.generateBoardPosId(ppx + 2, ppy), () -> {
								piece.moveTo(new Vector2f(ppx + 2, ppy));
								castle.moveTo(new Vector2f(ppx + 1, ppy));
								swapSides(piece.getColor());
							});
						}
					}
				}
			}
		}
	}

	private ArrayList<Integer> generateTakeMoves(ChessPiece piece) {
		ArrayList<Integer> possibleMoveIds = new ArrayList<>();
		int ppx = (int) piece.getPosition().x();
		int ppy = (int) piece.getPosition().y();
		switch (piece.getType()) {
			case PAWN -> {
				int dir = piece.getColor() == ChessPiece.Color.WHITE ? 1 : -1;
				if (getPieceAtPosition(ppx + 1, ppy + dir) != null && ppx + 1 <= 8 && ppy + dir > 0 && ppy + dir <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + dir));
				}
				if (getPieceAtPosition(ppx - 1, ppy + dir) != null && ppx - 1 > 0 && ppy + dir > 0 && ppy + dir <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + dir));
				}
				// En passant
				if (piece.getColor() == ChessPiece.Color.WHITE) {
					if (ppy == 5 && getPieceAtPosition(ppx + 1, ppy) != null) {
						possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy));
					}
					if (ppy == 5 && getPieceAtPosition(ppx - 1, ppy) != null) {
						possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy));
					}
				} else {
					if (ppy == 4 && getPieceAtPosition(ppx + 1, ppy) != null) {
						possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy));
					}
					if (ppy == 4 && getPieceAtPosition(ppx - 1, ppy) != null) {
						possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy));
					}
				}
			}
			case ROOK -> {
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8) {
						if (getPieceAtPosition(ppx + i, ppy) == null || getPieceAtPosition(ppx + i, ppy).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0) {
						if (getPieceAtPosition(ppx - i, ppy) == null || getPieceAtPosition(ppx - i, ppy).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppy + i <= 8) {
						if (getPieceAtPosition(ppx, ppy + i) == null || getPieceAtPosition(ppx, ppy + i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + i));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppy + i > 0) {
						if (getPieceAtPosition(ppx, ppy - i) == null || getPieceAtPosition(ppx, ppy - i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy - i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy - i));
							break;
						}
					} else {
						break;
					}
				}
			}
			case KNIGHT -> {
				if (ppx + 2 <= 8 && ppy + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 2, ppy + 1));
				}
				if (ppx + 2 <= 8 && ppy - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 2, ppy - 1));
				}
				if (ppx - 2 > 0 && ppy + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 2, ppy + 1));
				}
				if (ppx - 2 > 0 && ppy - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 2, ppy - 1));
				}
				if (ppx + 1 <= 8 && ppy + 2 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + 2));
				}
				if (ppx + 1 <= 8 && ppy - 2 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy - 2));
				}
				if (ppx - 1 > 0 && ppy + 2 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + 2));
				}
				if (ppx - 1 > 0 && ppy - 2 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy - 2));
				}
			}
			case BISHOP -> {
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8 && ppy + i <= 8) {
						if (getPieceAtPosition(ppx + i, ppy + i) == null || getPieceAtPosition(ppx + i, ppy + i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy + i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy + i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8 && ppy - i > 0) {
						if (getPieceAtPosition(ppx + i, ppy - i) == null || getPieceAtPosition(ppx + i, ppy - i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy - i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy - i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0 && ppy - i > 0) {
						if (getPieceAtPosition(ppx - i, ppy - i) == null || getPieceAtPosition(ppx - i, ppy - i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy - i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy - i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0 && ppy + i <= 8) {
						if (getPieceAtPosition(ppx - i, ppy + i) == null || getPieceAtPosition(ppx - i, ppy + i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy + i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy + i));
							break;
						}
					} else
						break;
				}
			}
			case QUEEN -> {
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8) {
						if (getPieceAtPosition(ppx + i, ppy) == null || getPieceAtPosition(ppx + i, ppy).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0) {
						if (getPieceAtPosition(ppx - i, ppy) == null || getPieceAtPosition(ppx - i, ppy).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppy + i <= 8) {
						if (getPieceAtPosition(ppx, ppy + i) == null || getPieceAtPosition(ppx, ppy + i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + i));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppy + i > 0) {
						if (getPieceAtPosition(ppx, ppy - i) == null || getPieceAtPosition(ppx, ppy - i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy - i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy - i));
							break;
						}
					} else {
						break;
					}
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8 && ppy + i <= 8) {
						if (getPieceAtPosition(ppx + i, ppy + i) == null || getPieceAtPosition(ppx + i, ppy + i).getType() == ChessPiece.Type.KING) {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy + i));
						} else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy + i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx + i <= 8 && ppy - i > 0) {
						if (getPieceAtPosition(ppx + i, ppy - i) == null || getPieceAtPosition(ppx + i, ppy - i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy - i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + i, ppy - i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0 && ppy - i > 0) {
						if (getPieceAtPosition(ppx - i, ppy - i) == null || getPieceAtPosition(ppx - i, ppy - i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy - i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy - i));
							break;
						}
					} else
						break;
				}
				for (int i = 1; i <= 7; i++) {
					if (ppx - i > 0 && ppy + i <= 8) {
						if (getPieceAtPosition(ppx - i, ppy + i) == null || getPieceAtPosition(ppx - i, ppy + i).getType() == ChessPiece.Type.KING)
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy + i));
						else {
							possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - i, ppy + i));
							break;
						}
					} else
						break;
				}
			}
			case KING -> {
				if (ppx + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy));
				}
				if (ppx - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy));
				}
				if (ppx + 1 <= 8 && ppy + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy + 1));
				}
				if (ppx - 1 > 8 && ppy - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy - 1));
				}
				if (ppy + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy + 1));
				}
				if (ppy - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx, ppy - 1));
				}
				if (ppx + 1 <= 8 && ppy - 1 > 0) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx + 1, ppy - 1));
				}
				if (ppx - 1 > 0 && ppy + 1 <= 8) {
					possibleMoveIds.add(BoardFramebuffer.Data.generateBoardPosId(ppx - 1, ppy + 1));
				}
			}
		}
		return possibleMoveIds;
	}

	private void cullUnsafe() {
		if (selectedPiece != null) {
			if (selectedPiece.getType() == ChessPiece.Type.KING) {
				for (var piece : pieces.stream().filter(cp -> cp.getColor() != selectedPiece.getColor()).toList()) {
					var takes = generateTakeMoves(piece);
					for (var take : takes) {
						moveActions.remove(take);
					}
				}
			}
			// TODO Remove moves that don't take the checking piece or block the checking piece
		}
	}
}
