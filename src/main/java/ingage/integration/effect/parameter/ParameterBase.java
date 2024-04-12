package ingage.integration.effect.parameter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ingage.Util;
import ingage.integration.Integration;
import ingage.integration.effect.EffectBase;
import net.objecthunter.exp4j.Expression;

public abstract class ParameterBase<T, R> {

	public transient Integration integration;
	public transient EffectBase effect;
	public ParameterType type;
	public String id;
	public String display;
	public String description;
	public boolean required = true;
	public T defaultValue;
	
	public abstract T getDefault();
	public abstract void imGui();
	public abstract ParameterConfigBase<T, R> createConfig();
	
	public static class Deserializer implements JsonDeserializer<ParameterBase<?, ?>> {

		@Override
		public ParameterBase<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String typeString = obj.get("type").getAsString();
			ParameterType type = ParameterType.valueOf(typeString);
			
			return Util.GSON.fromJson(json, type.getParameterClass());
		}
	}
	
	public static abstract class ParameterConfigBase<T, R> {
		public final transient String uuid = UUID.randomUUID().toString();
		public transient ParameterBase<T, R> parameter;
		public String parameterID;
		public ParameterType type;
		public T value;
		
		public abstract T getValue();
		public abstract void setValue(T value);
		public abstract R evaluate(Map<String, Double> variables);
		public abstract void imGui();
		public abstract ParameterBase.ParameterConfigBase<T, R> clone();
		
		public ParameterBase<T, R> getParameter() {
			return this.parameter;
		}
		
		public static class Deserializer implements JsonDeserializer<ParameterConfigBase<?, ?>> {

			@Override
			public ParameterConfigBase<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				JsonObject obj = json.getAsJsonObject();
				String typeString = obj.get("type").getAsString();
				ParameterType type = ParameterType.valueOf(typeString);
				
				return Util.GSON.fromJson(json, type.getParameterConfigClass());
			}
		}
	}
}
