package terrain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import textures.ModelTexture;
import textures.TerrainTexturePack;
import toolbox.Mathematics;
import models.RawModel;

public class Terrain {

	private static final float SIZE = 800;
	private static final float MAX_HEIGHT = 40;
	private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
	private static final int PERLIN_RADIUS = 50;
	private static final int PERLIN_OCTAVES = 1;
	
	private float x;
	private float z;
	
	private int vertexCount;
	
	private float heights[][];
	float[] vertices;
	float[] normals;
	float[] textureCoords;
	int[] indices;
	
	private RawModel model;
	private TerrainTexturePack texturePack;
	private Loader loader;
	
	public Terrain(int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack, String heightMap) {
		this.texturePack = texturePack;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.loader = loader;
		this.model = generateTerrain(loader, heightMap);
	}
	
	private RawModel generateTerrain(Loader loader, String heightMap){
		
		BufferedImage image = null;
		try {
		image = ImageIO.read(new File("res/" + heightMap + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		vertexCount = image.getHeight();
		heights = new float[vertexCount][vertexCount];
		int count = vertexCount * vertexCount;
		vertices = new float[count * 3];
		normals = new float[count * 3];
		textureCoords = new float[count*2];
		indices = new int[6*(vertexCount-1)*(vertexCount*1)];
		int vertexPointer = 0;
		for(int i=0;i<vertexCount;i++){
			for(int j=0;j<vertexCount;j++){
				vertices[vertexPointer*3] = (float)j/((float)vertexCount - 1) * SIZE;
				float height = getHeight(j, i, image);
				heights[j][i] = height;
				vertices[vertexPointer*3+1] = height;
				vertices[vertexPointer*3+2] = (float)i/((float)vertexCount - 1) * SIZE;
				Vector3f normal = calculateNormal(j, i, image);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)vertexCount - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)vertexCount - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<vertexCount-1;gz++){
			for(int gx=0;gx<vertexCount-1;gx++){
				int topLeft = (gz*vertexCount)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*vertexCount)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadtoVAO(vertices, textureCoords, normals, indices);
	}
	
	public float getHeightOfTerrain(float worldX, float worldZ) {
		float terrainX = worldX - this.x;	//Where standing in relation to terrain block
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / ((float)heights.length - 1);	//Size of individual terrain square that makes up larger terrain block
		int gridX = (int)Math.floor(terrainX / gridSquareSize);		//Where standing on individual terrain square
		int gridZ = (int)Math.floor(terrainZ / gridSquareSize);		
		if(gridX >= heights.length - 1 || gridZ > heights.length -1 || gridX < 0 || gridZ < 0) {
			return 0;	//Not standing on terrain block
		}
		float xCoord = (terrainX % gridSquareSize)/gridSquareSize;	//Position in individual terrain square normalized to (0,0) -> (1,1)
		float zCoord = (terrainZ % gridSquareSize)/gridSquareSize;
		float answer;
		if (xCoord <= (1-zCoord)) {
			answer = Mathematics.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
							heights[gridX + 1 >= vertexCount ? vertexCount - 1 : gridX + 1][gridZ], 0), new Vector3f(0,
							heights[gridX][gridZ + 1 >= vertexCount ? vertexCount - 1 : gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		} else {
			answer = Mathematics.barryCentric(new Vector3f(1, heights[gridX + 1 >= vertexCount ? vertexCount - 1 : gridX + 1][gridZ], 0), new Vector3f(1,
							heights[gridX + 1 >= vertexCount ? vertexCount - 1 : gridX + 1][gridZ + 1 >= vertexCount ? vertexCount - 1 : gridZ + 1], 1), new Vector3f(0,
							heights[gridX][gridZ + 1 >= vertexCount ? vertexCount - 1 : gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		return answer;
	}
	
	private Vector3f calculateNormal(int x, int z, BufferedImage image) {
		float heightL = getHeight(x-1, z, image);
		float heightR = getHeight(x+1, z, image);
		float heightD = getHeight(x, z-1, image);
		float heightU = getHeight(x, z+1, image);
		
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD - heightU);
		normal.normalise();
		return normal;
	}
	
	private Vector3f calculateNewNormal(int x, int z) {
		float heightR;
		float heightL;
		float heightD;
		float heightU;
		
		if(x-1 < 0) {
			heightL = heights[0][z];
		} else {
			heightL = heights[x-1][z];
		}
		
		if(x+1 >= heights.length) {
			heightR = heights[heights.length - 1][z];
		} else {
			heightR = heights[x+1][z];
		}
		
		if(z-1 < 0) {
			heightD = heights[x][0];
		} else {
			heightD = heights[x][z-1];
		}
		
		if(z+1 >= heights[0].length) {
			heightU = heights[x][heights[0].length -1];
		} else {
			heightU = heights[x][z + 1];
		}
		
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD - heightU);
		normal.normalise();
		return normal;
	}
	
	private float getHeight(int x, int z, BufferedImage image) {
		if(x < 0 || x>=image.getHeight() || z < 0 || z>=image.getHeight()) {
			return 0;
		}
		float height = image.getRGB(x, z);
		height += MAX_PIXEL_COLOUR/2f;
		height /= MAX_PIXEL_COLOUR/2f; //now height between -1 and 1
		height *= MAX_HEIGHT;
		return height;
	}
	
	public void perlinHeights(float worldX, float worldZ, boolean add){
		float terrainX = worldX - this.x;	//Where standing in relation to terrain block
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / ((float)heights.length - 1);	//Size of individual terrain square that makes up larger terrain block
		int gridX = (int)Math.floor(terrainX / gridSquareSize);		//Where standing on individual terrain square
		int gridZ = (int)Math.floor(terrainZ / gridSquareSize);		
		if(gridX >= heights.length - 1 || gridZ > heights.length -1 || gridX < 0 || gridZ < 0) {
			return;	//Not standing on terrain block
		}
		
		float[][] noise = Mathematics.generatePerlinNoise(PERLIN_RADIUS, PERLIN_RADIUS, PERLIN_OCTAVES);
		
		int startX = Math.max((int)(gridX - PERLIN_RADIUS/2f), 0);
		int endX = Math.min((int)(gridX + PERLIN_RADIUS/2f), vertexCount);
		int startZ = Math.max((int)(gridZ - PERLIN_RADIUS/2f), 0);
		int endZ = Math.min((int)(gridZ + PERLIN_RADIUS/2f), vertexCount);
		
		for(int i = 0; i + startX < endX; i++) {
			for(int j = 0; j + startZ < endZ; j++) {
				if(add) {
					heights[i + startX][j + startZ] += noise[j][i] * 2;	//multiply by a factor of 2 for a faster change
				} else {
					heights[i + startX][j + startZ] -= noise[j][i] * 2;
				}
				Vector3f newNormal = calculateNewNormal(i + startX, j + startZ);
				int vertexPointer = vertexCount*(startZ + j) + (startX + i);
				vertices[vertexPointer*3+1] = heights[i + startX][j + startZ];
				normals[vertexPointer*3] = newNormal.x;
				normals[vertexPointer*3+1] = newNormal.y;
				normals[vertexPointer*3+2] = newNormal.z;
			}
		}
		
		this.model = loader.replaceTerrainVAO(model.getVaoID(), vertices, textureCoords, normals, indices);
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public RawModel getModel() {
		return model;
	}

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}
	
	
	
	
}
