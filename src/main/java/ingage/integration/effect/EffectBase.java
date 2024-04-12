package ingage.integration.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ingage.Util;
import ingage.integration.Integration;
import ingage.integration.IntegrationManager;
import ingage.integration.effect.parameter.ParameterBase;
import ingage.integration.effect.parameter.ParameterBase.ParameterConfigBase;

public class EffectBase {

	public final transient String uuid = UUID.randomUUID().toString();
	public transient Integration integration;
	public String id;
	public String display;
	public List<ParameterBase<?, ?>> parameters = new ArrayList<ParameterBase<?, ?>>();
	
	public ParameterBase getParameter(String id) {
		for (ParameterBase parameter : this.parameters) {
			if (parameter.id.equals(id)) {
				return parameter;
			}
		}
		return null;
	}
	
	public EffectConfig createEffectConfig() {
		EffectConfig config = new EffectConfig();
		
		config.integrationID = this.integration.id;
		config.effectID = this.id;
		config.effectDisplay = this.display;
		
		for (ParameterBase<?, ?> p : this.parameters) {
			config.parameters.add(p.createConfig());
		}
		return config;
	}
	
	public static class EffectConfig {//TODO: When loading an effect config, add any new parameters
		public final transient String uuid = UUID.randomUUID().toString();
		public transient Integration integration;
		public transient EffectBase effect;
		public String integrationID;
		public String effectID;
		public String effectDisplay;
		public List<ParameterConfigBase<?, ?>> parameters = new ArrayList<ParameterConfigBase<?, ?>>();
		
		public Integration getIntegration() {
			if (this.integration == null) {
				this.integration = IntegrationManager.getIntegration(this.integrationID);
			}
			return this.integration;
		}
		
		public EffectBase getEffect() {
			Integration integration = this.getIntegration();
			
			if (integration != null) {
				if (this.effect == null) {
					this.effect = integration.getEffect(this.effectID);
				}
			}
			return this.effect;
		}
		
		public EffectMessage toEffectMessage(Map<String, Double> variables) {
			JsonObject json = new JsonObject();
			json.addProperty("integration", this.integrationID);
			json.addProperty("type", this.effectID);
			
			JsonObject parameters = new JsonObject();
			json.add("values", parameters);
			
			for (ParameterConfigBase<?, ?> parameter : this.parameters) {
//				Logger.log("Parameter "+parameter.parameterID+" "+Util.GSON.toJson(parameter.evaluate(variables)));
//				JsonObject p = new JsonObject();
				parameters.add(parameter.parameterID, Util.GSON.fromJson(Util.GSON.toJson(parameter.evaluate(variables)), JsonElement.class));
//				parameters.add(p);
			}
			return new EffectMessage(this.integrationID, json);
		}
		
		public void imGui() {
			for (ParameterConfigBase<?, ?> parameter : this.parameters) {
				parameter.imGui();
			}
		}
		
		public EffectConfig clone() {
			EffectConfig effect = new EffectConfig();
			effect.integration = this.integration;
			effect.effect = this.effect;
			effect.integrationID = this.integrationID;
			effect.effectID = this.effectID;
			effect.effectDisplay = this.effectDisplay;
			
			for (ParameterConfigBase<?, ?> parameter : this.parameters) {
				effect.parameters.add(parameter.clone());
			}
			return effect;
		}
	}
}
