package ingage;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ingage.event.EventBase;
import ingage.integration.condition.ConditionBase;
import ingage.integration.effect.parameter.ParameterBase;

public class Util {
	public static Gson GSON = new Gson().newBuilder().setPrettyPrinting().registerTypeAdapter(EventBase.class, new EventBase.Deserializer()).registerTypeAdapter(Instant.class, new InstantDeserializer()).registerTypeAdapter(ConditionBase.class, new ConditionBase.Deserializer()).registerTypeAdapter(ParameterBase.class, new ParameterBase.Deserializer()).registerTypeAdapter(ParameterBase.ParameterConfigBase.class, new ParameterBase.ParameterConfigBase.Deserializer()).create();
	
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
}
