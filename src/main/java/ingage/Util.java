package ingage;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.stb.STBImage;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ingage.event.EventBase;
import ingage.integration.condition.ConditionBase;
import ingage.integration.effect.EffectBase.EffectConfig;
import ingage.integration.effect.parameter.ParameterBase;

public class Util {
	public static Gson GSON = new Gson().newBuilder().setPrettyPrinting().registerTypeAdapter(EventBase.class, new EventBase.Deserializer()).registerTypeAdapter(Instant.class, new InstantDeserializer()).registerTypeAdapter(ConditionBase.class, new ConditionBase.Deserializer()).registerTypeAdapter(ParameterBase.class, new ParameterBase.Deserializer()).registerTypeAdapter(ParameterBase.ParameterConfigBase.class, new ParameterBase.ParameterConfigBase.Deserializer()).create();
	public static final Random random = new Random();
	
	public static class InstantDeserializer implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
		@Override
		public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Instant.parse(json.getAsString());
		}

		@Override
		public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
			return context.serialize(src.toString());
		}
	}
	
	public static int weightedRandomIndex(List<EffectConfig> value, Random rand) {
		int i;

		// Create array of weights, combining previous weights into the current
		int[] weights = new int[value.size()];
		weights[0] = value.get(0).weight;

		for (i = 1; i < value.size(); i++) {
			weights[i] = value.get(i).weight + weights[i - 1];
		}

		// Create random number from 0 inclusive to total weight exclusive
		int random = rand.nextInt(weights[weights.length - 1]);

		// Find first weight larger than the random number
		for (i = 0; i < weights.length; i++) {
			if (weights[i] > random) {
				break;
			}
		}
		return i;
	}
	
	public static void setWindowIcon(long window, String... paths) throws Exception {
		Buffer imageBuffer = GLFWImage.malloc(paths.length);
		
		List<GLFWImage> structs = new ArrayList<GLFWImage>();
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		
		for (int i = 0; i < paths.length; i++) {
			InputStream stream = Util.class.getClassLoader().getResourceAsStream(paths[i]);
			byte[] bytes = stream.readAllBytes();
			ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
			int[] widthBuff = new int[1];
			int[] heightBuff = new int[1];
			int[] channelsBuff = new int[1];
			
			ByteBuffer pixels = STBImage.stbi_load_from_memory(buffer, widthBuff, heightBuff, channelsBuff, 4);
			
			if (pixels == null) {
				throw new Exception();
			}
			pixels.flip();
			
			int width = widthBuff[0];
			int height = heightBuff[0];
			
			GLFWImage image = GLFWImage.malloc().set(width, height, pixels);
			imageBuffer.put(i, image);
			
			structs.add(image);
			buffers.add(pixels);
		}

    	GLFW.glfwSetWindowIcon(window, imageBuffer);
    	
    	//Free memory
    	for (int i = 0; i < paths.length; i++) {
    		structs.get(i).free();
			STBImage.stbi_image_free(buffers.get(i));
    	}
    	imageBuffer.free();
	}
}
