package renderEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import models.RawModel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Loader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> terrainVbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	public RawModel loadtoVAO (float[] positions, float[] textureCoordinates, float[] normals, int[] indicies) {
		int vaoID = createVAO();
		bindIndiciesBuffer(indicies, false);
		storeDataInAttributeList(0, 3, positions, false);
		storeDataInAttributeList(1, 2, textureCoordinates, false);
		storeDataInAttributeList(2, 3, normals, false);
		unbindVAO();
		return new RawModel(vaoID, indicies.length);
	}
	
	public RawModel replaceTerrainVAO(int vaoID, float[] positions, float[] textureCoordinates, float[] normals, int[] indicies) {
		
		GL30.glDeleteVertexArrays(vaoID);
		vaos.remove(vaos.indexOf(vaoID));
		
		for(int vbo : terrainVbos) {
			GL15.glDeleteBuffers(vbo);
		}
		
		int newVaoID = createVAO();
		bindIndiciesBuffer(indicies, true);
		storeDataInAttributeList(0, 3, positions, true);
		storeDataInAttributeList(1, 2, textureCoordinates, true);
		storeDataInAttributeList(2, 3, normals, true);
		unbindVAO();
		return new RawModel(newVaoID, indicies.length);
	}
	
	public int loadTexture(String fileName) {
		Texture texture = null;
		try {
		texture = TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 catch (IOException e) {
			 e.printStackTrace();
		 }
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}
	
	public void cleanUp() {
		for(int vao : vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for(int texture : textures) {
			GL11.glDeleteTextures(texture);
		}
	}
	
	//Create VAO, bind (activate) it and return it's ID so we can reference it
	private int createVAO(){
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data, boolean terrain) {
		int vboID = GL15.glGenBuffers();	//Create VBO, return ID
		if(terrain) {
			terrainVbos.add(vboID);
		} else {
			vbos.add(vboID);
		}
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);	//Bind VBO to modify it, VBO type is Array Buffer, pass ID to bind given VBO
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);	//It's an array Buffer, with data in FloatBuffer buffer, and we don't want to edit the buffer ever again, aka data is static (GL_STATIC_DRAW)
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);	//AttributeListNumber, # of coords per vertex (x,y,z) = 3 TextureCoordinates - (u,v) = 2 , data is floats, data normalized? no, distance (buffer) between verticies in array (none), data offset (start at beginning) = 0
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);	//Unbinds VBO
	}
	
	//VBOs use FloatBuffers
	private FloatBuffer storeDataInFloatBuffer(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();	//flip sets up the buffer so it could be read
		return buffer;
	}
	
	private void bindIndiciesBuffer(int[] indicies, boolean terrain) {
		int vboID = GL15.glGenBuffers();
		if(terrain) {
			terrainVbos.add(vboID);
		} else {
			vbos.add(vboID);
		}
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);	//ELEMENT_ARRAY_BUFFER let's openGL know it's the indicies buffer (it's doesn't contain data)
		IntBuffer buffer = storeDataInIntBuffer(indicies);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);	//Binding an index buffer binds it to the currently bound VAO, so unbinding it remvoes it from the VAO
		//Don't unbind Index Buffer!    
		
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();	//so it can be read
		return buffer;
	}
	
	//Unbinds currently bound VAO
	private void unbindVAO(){
		GL30.glBindVertexArray(0);
	}

}
