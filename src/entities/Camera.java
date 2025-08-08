package entities;

import org.joml.Vector3f;

import renderEngine.DisplayManager;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
	
	private Vector3f position = new Vector3f(0,0,0);
	private float pitch;
	private float yaw;
	private float roll;
	
	public Camera() {}
	
	public void move() {
		if (glfwGetKey(DisplayManager.getWindow(), GLFW_KEY_W) == GLFW_PRESS) {
			position.z-=0.02f;
		}
		if (glfwGetKey(DisplayManager.getWindow(), GLFW_KEY_S) == GLFW_PRESS) {
			position.z+=0.02f;
		}
		if (glfwGetKey(DisplayManager.getWindow(), GLFW_KEY_D) == GLFW_PRESS) {
			position.x+=0.02f;
		}
		if (glfwGetKey(DisplayManager.getWindow(), GLFW_KEY_A) == GLFW_PRESS) {
			position.x-=0.02f;
		}
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	
	
	

}
