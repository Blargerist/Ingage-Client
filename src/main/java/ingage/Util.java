package ingage;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Random;

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
}
