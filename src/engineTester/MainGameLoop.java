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
		
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		for(int i=0;i<500;i++){
			entities.add(new Entity(staticTreeModel, new Vector3f(random.nextFloat()*800 - 400,0,random.nextFloat() * -600),0,0,0,3));
			entities.add(new Entity(staticGrassModel, new Vector3f(random.nextFloat()*400 - 200,0,random.nextFloat() * -600),0,0,0,2));
		}
		
		Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		// Build a 5x5 grid centered on (0,0)
		List<Terrain> terrains = new ArrayList<>();
		ModelTexture grassTex = new ModelTexture(loader.loadTexture("grass"));

		for (int gz = -20; gz <= 20; gz++) {
		    for (int gx = -2; gx <= 2; gx++) {
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
