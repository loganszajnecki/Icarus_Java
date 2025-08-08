package renderEngine;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class DisplayManager {
	
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int FPS_CAP = 170;
	private static long window;
    private static double lastTime;
	
	public static void createDisplay() {
        // 1) init GLFW
        if ( !glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // 2) configure OpenGL context (3.2 core)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // 3) create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Icarus_Java", MemoryUtil.NULL, MemoryUtil.NULL);
        if ( window == MemoryUtil.NULL ) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // 4) make context current and create GL capabilities
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // 5) set the OpenGL viewport to cover the whole window
        glViewport(0, 0, WIDTH, HEIGHT);

        // 6) disable v-sync so we can manually cap FPS
        glfwSwapInterval(0);

        // init time for sync()
        lastTime = glfwGetTime();
    }
	
	public static void updateDisplay() {
        // swap the color buffers
        glfwSwapBuffers(window);
        // poll for window events (keyboard, mouse, etc.)
        glfwPollEvents();
        // cap the framerate
        sync(FPS_CAP);
    }
	
	public static void closeDisplay() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }
	
	/**
     * Simple loop-sync to cap to `fps` frames per second.
     * (mimics Display.sync in LWJGL2)
     */
    private static void sync(int fps) {
        double loopSlot = 1.0 / fps;
        double endTime  = lastTime + loopSlot;
        while ( glfwGetTime() < endTime ) {
            try {
                Thread.sleep(1);
            } catch ( InterruptedException ignored ) { }
        }
        lastTime = glfwGetTime();
    }
    
    public static boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }
    
    public static float getAspectRatio() {
        int[] width  = new int[1];
        int[] height = new int[1];
        // Use framebuffer size for correct OpenGL viewport scaling on HiDPI
        glfwGetFramebufferSize(window, width, height);
        return (float) width[0] / (float) height[0];
    }
    
    public static long getWindow() {
        return window;
    }

}
