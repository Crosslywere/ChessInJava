package com.crossly.engine.graphics;

import com.crossly.engine.Engine;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;

public class Model {

	private final ArrayList<TexturedMesh> meshes = new ArrayList<>();

	private String directory;

	public Model(String filepath) {
		loadModel(Engine.getAbsolutePath(filepath));
	}

	private void loadModel(String filepath) {
		try (AIScene scene = Assimp.aiImportFile(filepath, Assimp.aiProcess_Triangulate | Assimp.aiProcess_RemoveRedundantMaterials)) {
			if (scene == null)
				throw new RuntimeException(Assimp.aiGetErrorString());

			AINode node;
			if (scene.mFlags() == Assimp.AI_SCENE_FLAGS_INCOMPLETE || (node = scene.mRootNode()) == null)
				throw new RuntimeException(Assimp.aiGetErrorString());

			if (filepath.contains("/"))
				directory = filepath.substring(0, filepath.lastIndexOf("/"));
			if (filepath.contains("\\"))
				directory = filepath.substring(0, filepath.lastIndexOf("\\"));

			processNode(node, scene);
		}
	}

	private void processNode(AINode node, AIScene scene) {
		for (int i = 0; i < node.mNumMeshes(); i++) {
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(node.mMeshes().get(i)));
			this.meshes.add(processMesh(mesh, scene));
		}
		for (int i = 0; i < node.mNumChildren(); i++) {
			processNode(AINode.create(node.mChildren().get(i)), scene);
		}
	}

	private TexturedMesh processMesh(AIMesh mesh, AIScene scene) {
		float[] posData = new float[mesh.mNumVertices() * 3];
		float[] cordData = new float[mesh.mNumVertices() * 2];
		AIVector3D.Buffer cordBuffer = mesh.mTextureCoords(0);
		float[] normData = new float[mesh.mNumVertices() * 3];
		AIVector3D.Buffer normBuffer = mesh.mNormals();
		for (int i = 0; i < mesh.mNumVertices(); i++) {
			AIVector3D pos = mesh.mVertices().get(i);
			posData[i * 3] = pos.x();
			posData[1+i*3] = pos.y();
			posData[2+i*3] = pos.z();
			if (cordBuffer == null) {
				cordData = null;
			} else {
				cordData[i * 2] = cordBuffer.get(i).x();
				cordData[1+i*2] = cordBuffer.get(i).y();
			}
			if (normBuffer == null) {
				normData = null;
			} else {
				normData[i * 3] = normBuffer.get(i).x();
				normData[1+i*3] = normBuffer.get(i).y();
				normData[2+i*3] = normBuffer.get(i).z();
			}
		}
		ArrayList<Integer> indicesData = new ArrayList<>();
		for (int i = 0; i < mesh.mNumFaces(); i++) {
			AIFace face = mesh.mFaces().get(i);
			for (int j = 0; j < face.mNumIndices(); j++) {
				indicesData.add(face.mIndices().get(j));
			}
		}
		int[] indices = new int[indicesData.size()];
		for (int i = 0; i < indicesData.size(); i++) {
			indices[i] = indicesData.get(i);
		}
		ImageTexture diffuseTexture = null;
		ImageTexture specularTexture = null;
		ImageTexture ambientTexture = null;
		PointerBuffer materialPointers;
		if (mesh.mMaterialIndex() >= 0 && (materialPointers = scene.mMaterials()) != null) {
			AIMaterial material = AIMaterial.create(materialPointers.get(mesh.mMaterialIndex()));
			diffuseTexture = loadTexture(material, Assimp.aiTextureType_DIFFUSE);
			specularTexture = loadTexture(material, Assimp.aiTextureType_SPECULAR);
			ambientTexture = loadTexture(material, Assimp.aiTextureType_AMBIENT);
		}
		return new TexturedMesh(new Mesh(posData, cordData, normData, indices, true), diffuseTexture, specularTexture, ambientTexture);
	}

	private ImageTexture loadTexture(AIMaterial material, int type) {
		AIString path = AIString.calloc();
		Assimp.aiGetMaterialTexture(material, type, 0, path, (IntBuffer) null, null, null, null, null, null);
		String source;
		if (!(source = path.dataString().trim()).isEmpty()) {
			String texturePath = directory + '/' + source + ".png";
			return new ImageTexture(texturePath, true, true);
		}
		return null;
	}

	public void draw(Shader shader) {
		meshes.forEach(mesh -> mesh.draw(shader));
	}

	public void delete() {
		meshes.forEach(TexturedMesh::delete);
	}

	private record TexturedMesh(
			Mesh mesh,
			ImageTexture diffuseTexture,
			ImageTexture specularTexture,
			ImageTexture ambientTexture
	) {
		public void draw(Shader shader) {
			if (diffuseTexture != null) {
				diffuseTexture.bind(0);
				shader.setInt("uMaterial.diffuse", 0);
			}
			if (specularTexture != null) {
				specularTexture.bind(1);
				shader.setInt("uMaterial.specular", 1);
			}
			if (ambientTexture != null) {
				ambientTexture.bind(2);
				shader.setInt("uMaterial.ambient", 2);
			}
			mesh.draw();
		}

		public void delete() {
			mesh.delete();
			if (diffuseTexture != null) {
				diffuseTexture.delete();
			}
			if (specularTexture != null) {
				specularTexture.delete();
			}
			if (ambientTexture != null) {
				ambientTexture.delete();
			}
		}
	}
}
