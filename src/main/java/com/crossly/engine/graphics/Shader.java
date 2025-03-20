package com.crossly.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.lwjgl.opengl.GL33.*;
import static com.crossly.engine.Engine.*;

public class Shader {

	private final int program;
	private final ArrayList<Integer> shaderParts = new ArrayList<>();
	private final Map<String, Integer> uniformMap = new HashMap<>();

	public Shader(String vert, String frag, boolean isFile) {
		program = glCreateProgram();
		int vs, fs;
		if (isFile) {
			vs = createShader(getSourceUnpacked(getAbsolutePath(vert), new ArrayList<>()), GL_VERTEX_SHADER);
			fs = createShader(getSourceUnpacked(getAbsolutePath(frag), new ArrayList<>()), GL_FRAGMENT_SHADER);
		} else {
			vs = createShader(vert, GL_VERTEX_SHADER);
			fs = createShader(frag, GL_FRAGMENT_SHADER);
		}
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		shaderParts.add(vs);
		shaderParts.add(fs);
		glLinkProgram(program);
		validate();
	}

	public Shader(String vertPath, String fragPath) {
		this(vertPath, fragPath, true);
	}

	public void use() {
		glUseProgram(program);
	}

	public void delete() {
		shaderParts.forEach(GL33::glDeleteShader);
		glDeleteProgram(program);
	}

	public void setInt(String name, int value) {
		glUniform1i(getUniformLocation(name), value);
	}

	public void setFloat(String name, float value) {
		glUniform1f(getUniformLocation(name), value);
	}

	public void setFloat2(String name, Vector2f value) {
		glUniform2f(getUniformLocation(name), value.x, value.y);
	}

	public void setFloat3(String name, Vector3f value) {
		glUniform3f(getUniformLocation(name), value.x, value.y, value.z);
	}

	public void setMatrix4(String name, Matrix4f value) {
		float[] matrix = new float[16];
		value.get(matrix);
		glUniformMatrix4fv(getUniformLocation(name), false, matrix);
	}

	private static int createShader(String source, int type) {
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
		int success = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (success == 0)
			throw new RuntimeException(glGetShaderInfoLog(shader));
		return shader;
	}

	private static String getSourceUnpacked(String path, ArrayList<String> includedPaths) {
		try {
			String source = new String(Files.readAllBytes(Paths.get(path)));
			if (!source.contains("#include"))
				return source;
			Scanner scn = new Scanner(source);
			StringBuilder builder = new StringBuilder();
			while (scn.hasNextLine()) {
				String line = scn.nextLine().trim();
				if (line.startsWith("#include")) {
					String includeFile = line.substring(9).replace('"', ' ').trim();
					if (includedPaths.contains(includeFile))
						continue;
					includedPaths.add(includeFile);
					builder.append(getSourceUnpacked(path.substring(0, path.lastIndexOf('/') + 1) + includeFile, includedPaths))
							.append('\n');
				} else
					builder.append(line).append('\n');
			}
			return builder.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void validate() {
		int success = glGetProgrami(program, GL_LINK_STATUS);
		if (success == 0)
			throw new RuntimeException(glGetProgramInfoLog(program));
	}

	private int getUniformLocation(String name) {
		if (uniformMap.containsKey(name))
			return uniformMap.get(name);
		int location = glGetUniformLocation(program, name);
		if (location < 0)
			System.out.println("uniform '" + name + "' is unavailable in shader!");
		uniformMap.put(name, location);
		return location;
	}
}
