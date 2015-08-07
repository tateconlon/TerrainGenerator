package renderEngine;

import java.util.List;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import shaders.TerrainShader;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexturePack;
import toolbox.Mathematics;

public class TerrainRenderer {
	
	private TerrainShader shader;
	
	public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextureUnits();
		shader.stop();
	}
	
	public void render(List<Terrain> terrains){
		for(Terrain terrain : terrains) {
			prepareTexturedModel(terrain);
			loadTransformationMatrix(terrain);
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
	}
	
	//Bind texture and load uniform variables into shader
		private void prepareTexturedModel(Terrain terrain){
			RawModel rawModel = terrain.getModel();
			
			GL30.glBindVertexArray(rawModel.getVaoID());
			GL20.glEnableVertexAttribArray(0);	//We put model data in 0
			GL20.glEnableVertexAttribArray(1);  //Texture coordinate in 1\
			GL20.glEnableVertexAttribArray(2);  //Normal vectors in 2\
			
			shader.loadSpecularVariables(1, 0);
			bindTexture(terrain);
		}
		
	private void bindTexture(Terrain terrain){
		TerrainTexturePack texturePack = terrain.getTexturePack();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);	//sampler2D defaults to texture in texture bank 0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getGrass().getTextureID());	//Bind texture so sampler2D use it

		GL13.glActiveTexture(GL13.GL_TEXTURE1);	//sampler2D defaults to texture in texture bank 0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getRock().getTextureID());	//Bind texture so sampler2D use it
		

		GL13.glActiveTexture(GL13.GL_TEXTURE2);	//sampler2D defaults to texture in texture bank 0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getSnow().getTextureID());	//Bind texture so sampler2D use it
		

		GL13.glActiveTexture(GL13.GL_TEXTURE3);	//sampler2D defaults to texture in texture bank 0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getWater().getTextureID());	//Bind texture so sampler2D use it
	}
		//unbind the texture
		private void unbindTexturedModel() {
			GL20.glDisableVertexAttribArray(0);	//Disable vertex attribute
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL30.glBindVertexArray(0);	//Unbind vertex array
		}
		
		//Load transformation matrix specific to entity into shader
		private void loadTransformationMatrix(Terrain terrain) {
			Matrix4f transformationMatrix = Mathematics.createTransformationMatrix(new Vector3f(terrain.getX(), 0f, terrain.getZ()), 0, 0, 0, 1);
			shader.loadTransformationMatrix(transformationMatrix);
		}

}
