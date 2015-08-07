package textures;

public class TerrainTexturePack {

	private TerrainTexture grass;	
	private TerrainTexture rock;
	private TerrainTexture snow;
	private TerrainTexture water;
	
	public TerrainTexturePack(TerrainTexture grass, TerrainTexture rock,
			TerrainTexture snow, TerrainTexture water) {
		this.grass = grass;
		this.rock = rock;
		this.snow = snow;
		this.water = water;
	}
	public TerrainTexture getGrass() {
		return grass;
	}
	public TerrainTexture getRock() {
		return rock;
	}
	public TerrainTexture getSnow() {
		return snow;
	}
	public TerrainTexture getWater() {
		return water;
	}
	
	
}
