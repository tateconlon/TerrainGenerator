package toolbox;

import java.util.Random;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

public class Mathematics {
	
	private static Random random = new Random(System.currentTimeMillis());
	
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);	//scale uniformly in all axes
		return matrix;
	}
	
	//View Matrix moves everything in the opposite of the camera's position and rotation.  Roll first, then translate in opposite direction
	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}
	
	private static float linearInterpolation(float x0, float x1, float alpha)
	{
	   return x0 * (1 - alpha) + alpha * x1;
	}
	
	//Creates smooth noise from white noise by linearly interpolating 4 corners of sampling
	private static float[][] generateSmoothNoise(float[][] baseNoise, int octave)
	{
	   int width = baseNoise.length;
	   int height = baseNoise[0].length;
	   float[][] smoothNoise = new float[width][height];
	   int samplePeriod = 1 << octave; //2 ^ octave
	   float sampleFrequency = 1.0f / samplePeriod;
	 
	   for (int i = 0; i < width; i ++)  {
	      //Calculate the horizontal sampling indices
	      int sample_i0 = (i / samplePeriod) * samplePeriod;
	      int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
	      float horizontal_blend = (i - sample_i0) * sampleFrequency;
	 
	      for (int j = 0; j < height; j ++) {
	         //Calculate the vertical sampling indices
	         int sample_j0 = (j / samplePeriod) * samplePeriod;
	         int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
	         float vertical_blend = (j - sample_j0) * sampleFrequency;
	 
	         //Blend the top two corners
	         float top = linearInterpolation(baseNoise[sample_i0][sample_j0],
	            baseNoise[sample_i1][sample_j0], horizontal_blend);
	 
	         //Blend the bottom two corners
	         float bottom = linearInterpolation(baseNoise[sample_i0][sample_j1],
	            baseNoise[sample_i1][sample_j1], horizontal_blend);
	 
	         //Final blend
	         smoothNoise[i][j] = top;//linearInterpolation(top, bottom, vertical_blend);
	      }
	   }
	   return smoothNoise;
	}
	
	//Creates perlin noise from sampling a base noise function
	public static float[][] generatePerlinNoise(int width, int height, int octaveCount)
	{
		float[][] baseNoise = generateWhiteNoise(width, height);
	 
	   //Used to hold smooth noise of different octaves
	   float[][][] smoothNoise = new float[octaveCount][][];
	 
	   float persistance = 0.5f;
	 
	   //Generate smooth noise for each octave.  
	   //A lower octave is a higher frequency, and we start at a lower frequency then the possible highest
	   //as this makes a better looking terrain
	   for (int i = 0; i < octaveCount; i++) {
	       smoothNoise[i] = generateSmoothNoise(baseNoise, i+2);
	   }
	 
	    float[][] perlinNoise = new float[width][height];
	    float amplitude = 1.0f;
	    float totalAmplitude = 0.0f;
	 
	    //Blend noises together
	    for (int octave = octaveCount - 1; octave >= 0; octave--) {
	       amplitude *= persistance;
	       totalAmplitude += amplitude;
	 
	       for (int i = 0; i < width; i++) {
	          for (int j = 0; j < height; j++) {
	             perlinNoise[i][j] += (1-(float)Math.abs(smoothNoise[octave][i][j])) * amplitude;
	          }
	       }
	    }
	 
	    for(int i = 0; i < width; i++) {
	    	for(int j = 0; j < height; j++) {
	    		float i_component = (i - width/2f) * (i - width/2f);
	    		float j_component = (j - height/2f) * (j - height/2f);
	    		float distFromCenter = (float)Math.sqrt(i_component + j_component);
	    		float percentFromCenter = Math.min(distFromCenter / (width/2f), 1f);	//Cap at 100%
	    		//if(percentFromCenter > .5) {
	    			perlinNoise[i][j] *= Math.cos(percentFromCenter * Math.PI /2f) ;//* Math.cos(percentFromCenter * Math.PI /2f);
	    		//}
	    	}
	    }
	   return perlinNoise;
	}
	
	private static float[][] generateWhiteNoise(int width, int height)
	{
	    float[][] noise = new float[width][height];
	 
	    for (int i = 0; i < width; i++) {
	        for (int j = 0; j < height; j++) {
	            noise[i][j] = (float)random.nextDouble() % 1;
	        }
	    }
	    return noise;
	}

}
