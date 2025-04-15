package com.crossly.chess;

import com.crossly.engine.graphics.Model;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ChessPiece {

	public enum Type {
		PAWN("meshes/Pawn.obj"),
		ROOK("meshes/Rook.obj"),
		KNIGHT("meshes/Knight.obj"),
		BISHOP("meshes/Bishop.obj"),
		QUEEN("meshes/Queen.obj"),
		KING("meshes/King.obj");

		private final Model model;

		Type(String path) {
			model = new Model(path);
		}

		public Model getModel() {
			return model;
		}
	}

	public enum Color {
		WHITE(new Vector3f(.8f, .8f, .6f)),
		BLACK(new Vector3f(.4f));

		private final Vector3f color;

		Color(Vector3f color) {
			this.color = color;
		}

		public Vector3f getColor() {
			return color;
		}
	}

	private final int pieceId;
	private Type type;
	private final Color color;
	private Vector2f position;
	private boolean moved = false;
	private boolean inPlay = true;

	public ChessPiece(int pieceId, Type type, Color color, Vector2f position) {
		this.pieceId = pieceId;
		this.type = type;
		this.color = color;
		this.position = position;
	}

	public ChessPiece(String param, Color color) throws IllegalArgumentException {
		this.color = color;
		String[] params = param.split(" ");
		if (params.length < 4) {
			throw new IllegalArgumentException("Incomplete parameters for creating chess piece!");
		}
		try {
			type = Type.valueOf(params[0]);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid chess piece type '" + params[0] + "'");
		}
		try {
			pieceId = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid chess piece id '" + params[1] + "'");
		}
		String[] xyPos = params[2].split(",");
		if (xyPos.length != 2) {
			throw new IllegalArgumentException("Invalid chess piece pos data '" + params[2] + "'");
		}
		position = new Vector2f();
		try {
			position.x = Integer.parseInt(xyPos[0]);
			position.y = Integer.parseInt(xyPos[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid chess piece pos data '" + params[2] + "'");
		}
		if (params[3].equalsIgnoreCase("true")) {
			moved = true;
		} else if (params[3].equalsIgnoreCase("false")) {
			moved = false;
		} else {
			throw new IllegalArgumentException("Invalid chess piece moved '" + params[3] + "'");
		}
	}

	public int getPieceId() {
		return pieceId;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Color getColor() {
		return color;
	}

	public Vector2f getPosition() {
		return position;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public void moveTo(Vector2f position) {
		setPosition(position);
		this.moved = true;
	}

	public boolean isMoved() {
		return moved;
	}

	public boolean isInPlay() {
		return inPlay;
	}

	public void setInPlay(boolean inPlay) {
		this.inPlay = inPlay;
	}

	public static void destroyModels() {
		for (var type : Type.values()) {
			type.getModel().delete();
		}
	}

	@Override
	public String toString() {
		return type.name() + ' ' + pieceId + ' ' +
				(int) position.x() + ',' + (int) position.y() + ' ' + moved;
	}
}
