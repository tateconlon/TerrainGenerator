package engineTester;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;

public class MainDisplayLoop {

	public static void main(String[] args) {

			DisplayManager.createDisplay();
			Loader loader = new Loader();

			TerrainTexture grass = new TerrainTexture(loader.loadTexture("grass"));
			TerrainTexture rock = new TerrainTexture(loader.loadTexture("rock"));
			TerrainTexture snow = new TerrainTexture(loader.loadTexture("snow"));
			TerrainTexture water = new TerrainTexture(loader.loadTexture("water"));
			
			TerrainTexturePack texturePack = new TerrainTexturePack(grass, rock, snow, water);
			
			Light light = new Light(new Vector3f(0,5000,0), new Vector3f(1f, 1f, 1f));
			
			Terrain terrain = new Terrain(-1, -1, loader, texturePack, "heightMap");
			
			Camera camera = new Camera(new Vector3f(0, 350, 0),30 ,-30 ,0);
			MasterRenderer renderer = new MasterRenderer();
			
			MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
			
			while(!Display.isCloseRequested()) {
				camera.move();
				
				picker.update();	//After camera
				Vector3f terrainPoint = picker.getCurrentTerrainPoint();
				if(terrainPoint != null && Mouse.isButtonDown(0)) {
					terrain.perlinHeights(terrainPoint.x, terrainPoint.z, true);
				} else if(terrainPoint != null && Mouse.isButtonDown(1)) {
					terrain.perlinHeights(terrainPoint.x, terrainPoint.z, false);
				}
				renderer.processTerrain(terrain);
				renderer.render(light, camera);
				//logic
				//render
				DisplayManager.updateDisplay();
			}
			renderer.cleanUp();
			loader.cleanUp();
			DisplayManager.closeDisplay();

	}

}
