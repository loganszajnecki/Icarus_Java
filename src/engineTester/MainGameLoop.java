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

public class MainGameLoop {

	public static void main(String[] args) {
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		
		RawModel tree_model = OBJLoader.loadObjModel("tree", loader);
		RawModel grass_model = OBJLoader.loadObjModel("grassModel", loader);
		
		TexturedModel staticTreeModel = new TexturedModel(tree_model,new ModelTexture(loader.loadTexture("tree")));
		TexturedModel staticGrassModel = new TexturedModel(grass_model,new ModelTexture(loader.loadTexture("grassTexture")));
		staticGrassModel.getTexture().setHasTransparency(true);
		
		TexturedModel staticFernModel = new TexturedModel(OBJLoader.loadObjModel("fern", loader), new ModelTexture(loader.loadTexture("fern")));
		staticFernModel.getTexture().setHasTransparency(true);
		
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		for(int i=0;i<20;i++){
			entities.add(new Entity(staticTreeModel, new Vector3f(random.nextFloat()*100 - 100,0,random.nextFloat() * -100),0,0,0,3));
			for(int j=0;j<10;j++){
				entities.add(new Entity(staticGrassModel, new Vector3f(random.nextFloat()*100 - 100,0,random.nextFloat() * -100),0,0,0,1));
				entities.add(new Entity(staticFernModel, new Vector3f(random.nextFloat()*100 - 100,0,random.nextFloat() * -100),0,0,0,0.6f));
			}
		}
		
		Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		// Build a 5x5 grid centered on (0,0)
		List<Terrain> terrains = new ArrayList<>();
		ModelTexture grassTex = new ModelTexture(loader.loadTexture("grass"));

		for (int gz = -10; gz <= 0; gz++) {
		    for (int gx = -10; gx <= 0; gx++) {
		        terrains.add(new Terrain(gx, gz, loader, grassTex));
		    }
		}
		
		Camera camera = new Camera();	
		MasterRenderer renderer = new MasterRenderer();
		
		while(!DisplayManager.shouldClose()){
			camera.move();
			
			for (Terrain t : terrains) {
			    renderer.processTerrain(t);
			}
			for(Entity entity:entities){
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
