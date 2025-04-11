package com.crossly.chess;

import com.crossly.engine.graphics.Model;
import com.crossly.engine.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ChessPiece {

	private final int id;
	private Type type;
	private Vector2f position;
	private Vector3f color;

	private boolean selected = false;
	private boolean moved = false;
	private final Side side;

	public static final Model PAWN;
	public static final Model ROOK;
	public static final Model KNIGHT;
	public static final Model BISHOP;
	public static final Model QUEEN;
	public static final Model KING;

	static {
		PAWN = new Model("meshes/Pawn.obj");
		ROOK = new Model("meshes/Rook.obj");
		KNIGHT = new Model("meshes/Knight.obj");
		BISHOP = new Model("meshes/Bishop.obj");
		QUEEN = new Model("meshes/Queen.obj");
		KING = new Model("meshes/King.obj");
	}

	public ChessPiece(int id, Type type, Vector2f position, Side color) {
		this.id = id;
		this.type = type;
		this.position = position;
		this.color = color.value;
		this.side = color;
	}

	public int getId() {
		return id;
	}

	public void draw(Shader shader) {
		shader.use();
		shader.setMatrix4("u_Model", new Matrix4f().translate(position.x, 0f, position.y).scale(0.8f).rotateY((float)Math.toRadians(side == Side.WHITE && type == Type.KNIGHT ? -90 : 0)));
		shader.setInt("u_Id", id);
		shader.setFloat3("u_Color", color);
		type.model.draw(shader);
	}

	public Vector2f getPosition() {
		return position;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
		moved = true;
	}

	public void highLight() {
		selected = true;
		this.color = new Vector3f(.8f, .6f, 0);
	}

	public void deselect() {
		selected = false;
		this.color = side.value;
	}

	public Side getSide() {
		return side;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isMoved() {
		return moved;
	}

	public enum Side {
		WHITE(new Vector3f(.8f, .8f, .6f)),
		BLACK(new Vector3f(.4f));

		private final Vector3f value;

		Side(Vector3f value) {
			this.value = value;
		}
	}

	public enum Type {
		PAWN(ChessPiece.PAWN),
		ROOK(ChessPiece.ROOK),
		KNIGHT(ChessPiece.KNIGHT),
		BISHOP(ChessPiece.BISHOP),
		QUEEN(ChessPiece.QUEEN),
		KING(ChessPiece.KING);

		private final Model model;

		Type(Model model) {
			this.model = model;
		}
		public Model getModel() {
			return model;
		}
	}
}
