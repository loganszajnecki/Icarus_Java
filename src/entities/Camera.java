package entities;

import org.joml.Vector3f;

import renderEngine.DisplayManager;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
	
	private Vector3f position = new Vector3f(0,5,0);
	private float pitch;
	private float yaw;
	private float roll;
	
	// mouse state
	private boolean firstMouse = true;
	private double lastX, lastY;
	private float mouseSensitivity = 0.1f;
	private boolean playMode = true;
	private boolean gWasPressed = false;
	private boolean cursorInit = false;
	
	public Camera() {}
	
	public void move() {
	    long window = DisplayManager.getWindow();
	    
	    if (playMode && !cursorInit) {
	        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	        firstMouse = true;     // avoid a jump on the first read
	        cursorInit = true;
	    }

	    // --- toggle play mode with G ---
	    if (glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS) {
	        if (!gWasPressed) { // only toggle once per press
	            playMode = !playMode;
	            if (playMode) {
	                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	                firstMouse = true; // reset mouse delta so no jump
	            } else {
	                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
	            }
	        }
	        gWasPressed = true;
	    } else {
	        gWasPressed = false;
	    }

	    // --- only update yaw/pitch when in play mode ---
	    if (playMode) {
	        double[] mx = new double[1], my = new double[1];
	        glfwGetCursorPos(window, mx, my);
	        if (firstMouse) { lastX = mx[0]; lastY = my[0]; firstMouse = false; }
	        double dx = mx[0] - lastX, dy = my[0] - lastY;
	        lastX = mx[0]; lastY = my[0];

	        yaw   += (float)(dx * mouseSensitivity);
	        pitch += (float)(dy * mouseSensitivity);
	        if (pitch > 89.0f) pitch = 89.0f;
	        if (pitch < -89.0f) pitch = -89.0f;
	        // --- movement code (yaw-based WASD) ---
		    float speed = 0.2f;
		    float yawRad = (float)Math.toRadians(yaw);
		    Vector3f forward = new Vector3f((float)Math.sin(yawRad), 0, (float)-Math.cos(yawRad));
		    Vector3f right   = new Vector3f((float)Math.cos(yawRad), 0, (float)Math.sin(yawRad));

		    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) position.fma(speed, forward);
		    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) position.fma(-speed, forward);
		    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) position.fma(speed, right);
		    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) position.fma(-speed, right);
		    if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) position.y += speed;
		    if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) position.y -= speed;
	    } else {
	    	return;
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
