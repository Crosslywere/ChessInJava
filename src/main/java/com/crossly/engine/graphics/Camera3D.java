package com.crossly.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera3D {

	private Vector3f position = new Vector3f(0f);
	private final Vector3f front = new Vector3f(0f, 0f, 1f);
	private final Vector3f right = new Vector3f(1f, 0f, 0f);

	private float pitch = 0f;
	private float pitchLast = 0f;
	private float yaw = 0f;
	private float yawLast = 0f;

	private float aspect;
	private float fovy = 60f;

	private static final Vector3f WORLD_UP = new Vector3f(0f, 1f, 0f);

	public Camera3D(float x, float y, float z, float aspect) {
		this.position = new Vector3f(x, y, z);
		this.aspect = aspect;
		updateRotation();
		updateDirections();
		setAspect(aspect);
	}

	public Camera3D(Vector3f position, float aspect) {
		this.position = position;
		this.aspect = aspect;
		updateRotation();
		updateDirections();
	}

	public Camera3D(float aspect) {
		updateRotation();
		updateDirections();
		this.aspect = aspect;
	}

	public Vector3f getFront() {
		return new Vector3f(front);
	}

	public Vector3f getRight() {
		return new Vector3f(right);
	}

	public Vector3f getWorldUp() {
		return new Vector3f(WORLD_UP);
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}

	public void addPosition(Vector3f amount) {
		position.add(amount, position);
	}

	public void addPosition(float x, float y, float z) {
		position.add(new Vector3f(x, y, z));
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		updateRotation();
		updateDirections();
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
		updateRotation();
		updateDirections();
	}

	public float getFovY() {
		return fovy;
	}

	public void setFovY(float fovy) {
		this.fovy = fovy;
	}

	public void rotateBy(Vector2f rotation) {
		pitch += rotation.y;
		yaw += rotation.x;
		updateRotation();
		updateDirections();
	}

	public Matrix4f getLookMatrix() {
		return new Matrix4f().lookAt(position, position.add(front, new Vector3f()), WORLD_UP);
	}

	public void setAspect(float aspect) {
		this.aspect = aspect;
	}

	public Matrix4f getProjectionViewMatrix() {
		return new Matrix4f().perspective((float) Math.toRadians(fovy), aspect, 0.1f, 1000f).mul(getLookMatrix());
	}

	private void updateRotation() {
		if (yaw > 360f)
			yaw %= 360;
		else if (yaw < 0f)
			yaw = 360f + (yaw % 360);

		if (pitch > 360f)
			pitch %= 360;
		else if (pitch < 0f)
			pitch = 360f + (pitch % 360);
	}

	private void updateDirections() {
		float pitchDiff = pitch - pitchLast;
		pitchLast = pitch;
		float yawDiff = yaw - yawLast;
		yawLast = yaw;
		front.rotateX((float) Math.toRadians(pitchDiff), front).rotateY((float) Math.toRadians(yawDiff), front);
		front.cross(WORLD_UP, right);
	}
}
