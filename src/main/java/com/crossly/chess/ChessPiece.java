package com.crossly.chess;

import com.crossly.engine.graphics.Model;
import com.crossly.engine.graphics.Shader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ChessPiece {

	private final int id;
	private Model model;
	private Vector2f position;
	private Vector3f color;

	public static final Model PAWN;
//	public static final Model ROOK;
//	public static final Model KNIGHT;
//	public static final Model BISHOP;
//	public static final Model QUEEN;
//	public static final Model KING;

	static {
		PAWN = new Model("ChessPiece/ChessPiece.obj");
	}

	public ChessPiece(int id, Model model, Vector2f position, Vector3f color) {
		this.id = id;
		this.model = model;
		this.position = position;
		this.color = color;
	}

	public int getId() {
		return id;
	}

	public void draw(Shader shader) {
		shader.use();
		shader.setMatrix4("u_Model", new Matrix4f().translate(position.x, 0f, position.y).scale(0.8f));
		shader.setInt("u_Id", id);
		shader.setFloat3("u_Color", color);
		model.draw(shader);
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}

	public void setModel(String modelFilepath) {
		model.delete();
		model = new Model(modelFilepath);
	}
}
