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
}
