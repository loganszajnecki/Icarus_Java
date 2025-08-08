package renderEngine;

import models.RawModel;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized OBJ loader (Blender-style "v/vt/vn") that preserves the exact output
 * layout used by the legacy code: same vertex order, same index buffer, same
 * texture-coordinate V flip (1 - v), and same normals mapping.
 */
public final class OBJLoader {

    private OBJLoader() {
        // no instances
    }

    public static RawModel loadObjModel(String fileName, Loader loader) {
        // Legacy path convention preserved: "res/<name>.obj"
        Path objPath = Path.of("res", fileName + ".obj");

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> texcoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        float[] texArray = null;
        float[] normArray = null;

        try (BufferedReader br = Files.newBufferedReader(objPath)) {
            String line;
            boolean seenFaces = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // skip blanks and comments
                }

                if (line.startsWith("v ")) {
                    // vertex position
                    String[] t = splitWS(line);
                    // t[0] == "v"
                    float x = parseF(t[1]);
                    float y = parseF(t[2]);
                    float z = parseF(t[3]);
                    vertices.add(new Vector3f(x, y, z));
                } else if (line.startsWith("vt ")) {
                    // texture coord
                    String[] t = splitWS(line);
                    float u = parseF(t[1]);
                    float v = parseF(t[2]);
                    texcoords.add(new Vector2f(u, v));
                } else if (line.startsWith("vn ")) {
                    // normal
                    String[] t = splitWS(line);
                    float nx = parseF(t[1]);
                    float ny = parseF(t[2]);
                    float nz = parseF(t[3]);
                    normals.add(new Vector3f(nx, ny, nz));
                } else if (line.startsWith("f ")) {
                    // Allocate arrays on first face line (legacy behavior)
                    if (!seenFaces) {
                        seenFaces = true;
                        int vCount = vertices.size();
                        texArray = new float[vCount * 2];
                        normArray = new float[vCount * 3];
                    }

                    // Faces may be triangles or N-gons. Triangulate fan-wise: v0, v(i), v(i+1)
                    String[] t = splitWS(line);
                    // t[0] == "f", the rest are vertex refs "v/vt/vn"
                    if (t.length < 4) {
                        // Not enough verts for a face, ignore gracefully
                        continue;
                    }

                    // Build a small list of the per-corner strings
                    List<String> refs = new ArrayList<>(t.length - 1);
                    for (int i = 1; i < t.length; i++) {
                        refs.add(t[i]);
                    }

                    // Triangulate: (0, i, i+1)
                    for (int i = 1; i < refs.size() - 1; i++) {
                        processVertex(refs.get(0), indices, texcoords, normals, texArray, normArray);
                        processVertex(refs.get(i), indices, texcoords, normals, texArray, normArray);
                        processVertex(refs.get(i + 1), indices, texcoords, normals, texArray, normArray);
                    }
                }
                // other OBJ records (mtllib, usemtl, s, o, g) are safely ignored
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read OBJ: " + objPath.toString(), e);
        }

        // Flatten vertex positions (preserve the legacy ordering)
        float[] posArray = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f v = vertices.get(i);
            int base = i * 3;
            posArray[base] = v.x;
            posArray[base + 1] = v.y;
            posArray[base + 2] = v.z;
        }

        // Convert indices list to primitive int[]
        int[] indexArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }

        // Hand back exactly what the legacy code handed to Loader
        return loader.loadToVAO(
                posArray,
                texArray != null ? texArray : new float[0],
                indexArray
        );
    }

    // Parse one vertex reference "vIndex/vtIndex/vnIndex"
    // and write into indices, texArray (with V flipped), normArray.
    private static void processVertex(
            String ref,
            List<Integer> indices,
            List<Vector2f> texcoords,
            List<Vector3f> normals,
            float[] texArray,
            float[] normArray) {

        // Split but keep empty parts so "v//vn" is handled (vt missing)
        String[] parts = ref.split("/", -1);

        // OBJ is 1-based; convert to 0-based
        int vIdx = parseI(parts[0]) - 1;
        indices.add(vIdx);

        // vt index may be missing
        if (parts.length > 1 && !parts[1].isEmpty() && texArray != null && !texcoords.isEmpty()) {
            int vtIdx = parseI(parts[1]) - 1;
            if (vtIdx >= 0 && vtIdx < texcoords.size()) {
                Vector2f uv = texcoords.get(vtIdx);
                int base = vIdx * 2;
                texArray[base] = uv.x;
                texArray[base + 1] = 1.0f - uv.y; // legacy V flip preserved
            }
        }

        // vn index may be missing
        if (parts.length > 2 && !parts[2].isEmpty() && normArray != null && !normals.isEmpty()) {
            int vnIdx = parseI(parts[2]) - 1;
            if (vnIdx >= 0 && vnIdx < normals.size()) {
                Vector3f n = normals.get(vnIdx);
                int base = vIdx * 3;
                normArray[base] = n.x;
                normArray[base + 1] = n.y;
                normArray[base + 2] = n.z;
            }
        }
    }

    // Helpers (fast, allocation-free)
    private static String[] splitWS(String s) {
        // Split on one-or-more whitespace
        return s.split("\\s+");
    }

    private static float parseF(String s) {
        return Float.parseFloat(s);
    }

    private static int parseI(String s) {
        return Integer.parseInt(s);
    }
}






//package renderEngine;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.joml.Vector2f;
//import org.joml.Vector3f;
//
//import models.RawModel;
//
//public class OBJLoader {
//	
//	public static RawModel loadObjModel(String fileName, Loader loader) {
//		FileReader fr = null;
//		try {
//			fr = new FileReader(new File("res/"+fileName+".obj"));
//		} catch (FileNotFoundException e) {
//			System.err.println("Couldn't load file!");
//			e.printStackTrace();
//		}
//		
//		BufferedReader reader = new BufferedReader(fr);
//		
//		String line;
//		List<Vector3f> vertices = new ArrayList<Vector3f>();
//		List<Vector2f> textures = new ArrayList<Vector2f>();
//		List<Vector3f> normals = new ArrayList<Vector3f>();
//		List<Integer> indices = new ArrayList<Integer>();
//		float[] verticesArray = null;
//		float[] normalsArray = null;
//		float[] textureArray = null;
//		int[] indicesArray = null;
//		
//		try{
//			while(true) {
//				line = reader.readLine();
//				String[] currentLine = line.split(" ");
//				if(line.startsWith("v ")) {
//					Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),Float.parseFloat(currentLine[2]),Float.parseFloat(currentLine[3]));
//					vertices.add(vertex);
//				}else if(line.startsWith("vt ")) {
//					Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),Float.parseFloat(currentLine[2]));
//					textures.add(texture);
//				}else if(line.startsWith("vn ")) {
//					Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),Float.parseFloat(currentLine[2]),Float.parseFloat(currentLine[3]));
//					normals.add(normal);
//				}else if(line.startsWith("f ")) {
//					textureArray = new float[vertices.size()*2];
//					normalsArray = new float[vertices.size()*3];
//					break;
//				}
//			}
//			
//			while(line != null) {
//				if(!line.startsWith("f ")) {
//					line = reader.readLine();
//					continue;
//				}
//				String[] currentLine = line.split(" ");
//				String[] vertex1 = currentLine[1].split("/");
//				String[] vertex2 = currentLine[2].split("/");
//				String[] vertex3 = currentLine[3].split("/");
//				
//				processVertex(vertex1, indices, textures, normals, textureArray, normalsArray);
//				processVertex(vertex2, indices, textures, normals, textureArray, normalsArray);
//				processVertex(vertex3, indices, textures, normals, textureArray, normalsArray);
//				line = reader.readLine();
//			}
//			reader.close();
//			
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		verticesArray = new float[vertices.size()*3];
//		indicesArray = new int[indices.size()];
//		int vertexPointer = 0;
//		for(Vector3f vertex:vertices) {
//			verticesArray[vertexPointer++] = vertex.x;
//			verticesArray[vertexPointer++] = vertex.y;
//			verticesArray[vertexPointer++] = vertex.z;
//		}
//		for(int i=0;i<indices.size();i++) {
//			indicesArray[i] = indices.get(i);
//		}
//		return loader.loadToVAO(verticesArray, textureArray, indicesArray);
//	}
//	
//	private static void processVertex(String[] vertexData, List<Integer> indices, List<Vector2f> textures, List<Vector3f> normals, float[] textureArray, float[] normalsArray) {
//		int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
//		indices.add(currentVertexPointer);
//		Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1])-1);
//		textureArray[currentVertexPointer*2] = currentTex.x;
//		textureArray[currentVertexPointer*2+1] = 1 - currentTex.y;
//		Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2])-1);
//		normalsArray[currentVertexPointer*3] = currentNorm.x;
//		normalsArray[currentVertexPointer*3+1] = currentNorm.y;
//		normalsArray[currentVertexPointer*3+2] = currentNorm.z;
//		
//	}
//
//}



