package renderEngine;

import java.util.List;
import java.util.Map;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Mathematics;
import entities.Entity;

public class EntityRenderer {
	
	
	private StaticShader shader;
	
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	

	
	//Render model in order of textures in map. Only binding/unbinding each texture once
	public void render(Map<TexturedModel, List<Entity>> entities) {
		for(TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for(Entity entity : batch) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);	//Drawing triangles, # of verticies to render, type of data (now using indicies buffer), start at start of data
			}
			unbindTexturedModel();
		}
		
	}
	
	//Bind texture and load uniform variables into shader
	private void prepareTexturedModel(TexturedModel model){
		RawModel rawModel = model.getRawModel();
		ModelTexture texture = model.getTexture();
		
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);	//We put model data in 0
		GL20.glEnableVertexAttribArray(1);  //Texture coordinate in 1\
		GL20.glEnableVertexAttribArray(2);  //Normal vectors in 2\
		
		shader.loadSpecularVariables(texture.getShineDamper(), texture.getReflectivity());

		GL13.glActiveTexture(GL13.GL_TEXTURE0);	//sampler2D defaults to texture in texture bank 0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());	//Bind texture so sampler2D use it
	}
	
	//unbind the texture
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0);	//Disable vertex attribute
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);	//Unbind vertex array
	}
	
	//Load transformation matrix specific to entity into shader
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Mathematics.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
	}
	


}
