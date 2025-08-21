package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.joml.Vector3f;

import models.RawModel;
import models.TexturedModel;
import entities.Camera;
import entities.Entity;
import entities.Light;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		// Terrain texture stuff
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture,gTexture,bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		// end terrain texture stuff
		
		RawModel tree_model = OBJLoader.loadObjModel("tree", loader);
		TexturedModel staticTreeModel = new TexturedModel(tree_model, new ModelTexture(loader.loadTexture("tree")));
		
		TexturedModel staticGrassModel = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		staticGrassModel.getTexture().setHasTransparency(true);
		staticGrassModel.getTexture().setUseFakeLighting(true);

		TexturedModel staticFlowerModel = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("flower")));
		staticFlowerModel.getTexture().setHasTransparency(true);
		staticFlowerModel.getTexture().setUseFakeLighting(true);
		
		TexturedModel staticFernModel = new TexturedModel(OBJLoader.loadObjModel("fern", loader), new ModelTexture(loader.loadTexture("fern")));
		staticFernModel.getTexture().setHasTransparency(true);
		
		TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel("lowPolyTree", loader), new ModelTexture(loader.loadTexture("lowPolyTree")));
		
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
//		for (int i = 0; i < 100; i++) {
//			entities.add(new Entity(staticTreeModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 5f));
//			for(int j=0;j<5;j++){
//				entities.add(new Entity(staticGrassModel, new Vector3f(random.nextFloat()*800 - 400,0,random.nextFloat() * -600),0,0,0,1));
//				entities.add(new Entity(staticFernModel, new Vector3f(random.nextFloat()*800 - 400,0,random.nextFloat() * -600),0,0,0,0.6f));
//			}
//		}
		for (int i = 0; i < 400; ++i) {
			entities.add(new Entity(staticTreeModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 5f));
			entities.add(new Entity(staticGrassModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 1f));
			entities.add(new Entity(staticFernModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 0.6f));
			entities.add(new Entity(staticFlowerModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 3f));
			entities.add(new Entity(bobble, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 1f));
			
			entities.add(new Entity(staticTreeModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * 600), 0, 0, 0, 5f));
			entities.add(new Entity(staticGrassModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * 600), 0, 0, 0, 1f));
			entities.add(new Entity(staticFernModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * 600), 0, 0, 0, 0.6f));
			entities.add(new Entity(staticFlowerModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * 600), 0, 0, 0, 3f));
			entities.add(new Entity(bobble, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * 600), 0, 0, 0, 1f));

		}
		
		Light light = new Light(new Vector3f(20000, 20000, 20000), new Vector3f(1, 1, 1));

		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(new Terrain(0,0,loader,texturePack, blendMap));
		terrains.add(new Terrain(-1,0,loader,texturePack, blendMap));
		terrains.add(new Terrain(0,-1,loader,texturePack, blendMap));
		terrains.add(new Terrain(-1,-1,loader,texturePack, blendMap));

		Camera camera = new Camera();
		MasterRenderer renderer = new MasterRenderer();

		while (!DisplayManager.shouldClose()) {
			camera.move();

			for (Terrain terrain : terrains) {
				renderer.processTerrain(terrain);
			}

			for (Entity entity : entities) {
				renderer.processEntity(entity);
			}
			renderer.render(light, camera);
			DisplayManager.updateDisplay();
		}

		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
