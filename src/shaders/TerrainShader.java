package shaders;

import org.lwjgl.util.vector.Matrix4f;

import toolbox.Mathematics;
import entities.Camera;
import entities.Light;

public class TerrainShader extends ShaderProgram{



	private static final String VERTEX_FILE = "src/shaders/terrainVertexShader.txt";
	private static final String FRAGMENT_FILE = "src/shaders/terrainFragmentShader.txt";
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPos;
	private int location_lightColour;
	private int location_reflectivity;
	private int location_shineDamper;
	private int location_grassSampler;
	private int location_rockSampler;
	private int location_snowSampler;
	private int location_waterSampler;
	
	
	public TerrainShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0,  "position");
		super.bindAttribute(1,  "textureCoordinate");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_lightPos = super.getUniformLocation("lightPosition");
		location_lightColour = super.getUniformLocation("lightColour");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_grassSampler = super.getUniformLocation("grassSampler");
		location_rockSampler = super.getUniformLocation("rockSampler");
		location_snowSampler = super.getUniformLocation("snowSampler");
		location_waterSampler = super.getUniformLocation("waterSampler");
	}
	
	public void loadSpecularVariables(float damper, float reflectivity) {
		super.loadFloat(location_reflectivity, reflectivity);
		super.loadFloat(location_shineDamper, damper);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}
	
	public void loadTransformationMatrix(Matrix4f matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Mathematics.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	public void loadLight(Light light){
		super.loadVector(location_lightPos, light.getPosition());
		super.loadVector(location_lightColour, light.getColour());
	}
	
	public void connectTextureUnits(){
		super.loadInt(location_grassSampler, 0);
		super.loadInt(location_rockSampler, 1);
		super.loadInt(location_snowSampler, 2);
		super.loadInt(location_waterSampler, 3);
	}

}
