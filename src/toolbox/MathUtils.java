package toolbox;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;

public class MathUtils {
	
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		return new Matrix4f()
		        .identity()
		        .translate(translation)
		        .rotate((float) Math.toRadians(rx), 1f, 0f, 0f)
		        .rotate((float) Math.toRadians(ry), 0f, 1f, 0f)
		        .rotate((float) Math.toRadians(rz), 0f, 0f, 1f)
		        .scale(scale);
	}
	
	public static Matrix4f createViewMatrix(Camera camera) {
	    Vector3f cameraPos = camera.getPosition();

	    return new Matrix4f()
	        .identity()
	        .rotate((float) Math.toRadians(camera.getPitch()), 1f, 0f, 0f)  // Pitch (X axis)
	        .rotate((float) Math.toRadians(camera.getYaw()),   0f, 1f, 0f)  // Yaw (Y axis)
	        .translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);           // Move world opposite to camera
	}

}
