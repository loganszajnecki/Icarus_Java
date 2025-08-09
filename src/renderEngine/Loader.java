package renderEngine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Loader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0,3,positions);
		storeDataInAttributeList(1,2,textureCoords);
		storeDataInAttributeList(2,3,normals);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}
	
	public int loadTexture(String fileName) {
	    // Flip the image vertically to match OpenGL’s coordinate system
	    //STBImage.stbi_set_flip_vertically_on_load(true);

	    int width, height;
	    ByteBuffer imageData;

	    // Load image
	    try ( MemoryStack stack = MemoryStack.stackPush() ) {
	        IntBuffer w       = stack.mallocInt(1);
	        IntBuffer h       = stack.mallocInt(1);
	        IntBuffer channels= stack.mallocInt(1);

	        String path = "res/" + fileName + ".png";
	        imageData = STBImage.stbi_load(path, w, h, channels, 4);
	        if (imageData == null) {
	            throw new RuntimeException(
	                "Failed to load texture '" + path + "': " +
	                STBImage.stbi_failure_reason()
	            );
	        }

	        width  = w.get(0);
	        height = h.get(0);
	    }

	    // Generate and bind a new texture ID
	    int texID = GL11.glGenTextures();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

	    // Upload the pixel data
	    GL11.glTexImage2D(
	        GL11.GL_TEXTURE_2D,
	        0,
	        GL11.GL_RGBA8,
	        width,
	        height,
	        0,
	        GL11.GL_RGBA,
	        GL11.GL_UNSIGNED_BYTE,
	        imageData
	    );

	    // Generate mip-maps
	    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

	    // Set texture parameters
	    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
	    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
	    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

	    // Free the native image memory
	    STBImage.stbi_image_free(imageData);

	    // Keep track for cleanup
	    textures.add(texID);

	    return texID;
	}
	
	public void cleanUp() {
		for(int vao:vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo:vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for(int texture:textures) {
			GL11.glDeleteTextures(texture);
		}
	}
	
	// VAOs
	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	// VBOs
	private void storeDataInAttributeList(int attributeNumber, int coordinateSze, float[] data) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW); // not going to edit the data, static draw
		GL20.glVertexAttribPointer(attributeNumber, coordinateSze, GL11.GL_FLOAT, false, 0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // unbind current VBO
	}
	
	private void unbindVAO() {
		// unbinds the current bound VAO
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices) {
		int vboID = GL15.glGenBuffers(); // returns ID of VBO
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID); // GL_ELEMENT_ARRAY_BUFFER tells OpenGL this is the Index Buffer
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
	    buffer.put(data).flip();
	    return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data) {
	    // allocate a direct native buffer for 'data.length' floats
	    FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
	    buffer.put(data).flip();
	    return buffer;
	}

}
