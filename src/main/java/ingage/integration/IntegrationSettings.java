package ingage.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;

import ingage.Util;
import ingage.integration.effect.parameter.ParameterBase.ParameterConfigBase;

public class IntegrationSettings {

	public final transient String uuid = UUID.randomUUID().toString();
	public String id = null;
	public List<ParameterConfigBase<?, ?>> settings = new ArrayList<ParameterConfigBase<?, ?>>();
	
	public void imGui(Integration integration) {
		if (settings.size() > 0) {
			//Render settings
			for (ParameterConfigBase<?, ?> setting : settings) {
				setting.imGui(null);
			}
		}
	}
	
	public IntegrationSettings clone() {
		IntegrationSettings settings = new IntegrationSettings();
		settings.id = this.id;
		settings.settings = new ArrayList<ParameterConfigBase<?, ?>>(this.settings.stream().map((e) -> { return e.clone(); }).toList());
		
		return settings;
	}
	
	public JsonObject toJsonPayload() {
		JsonObject json = new JsonObject();
		Map<String, Double> variables = new HashMap<String, Double>();
		
		json.addProperty("integration_id", this.id);
		
		for (ParameterConfigBase<?, ?> parameter : this.settings) {
			json.add(parameter.parameterID, Util.GSON.toJsonTree(parameter.evaluate(variables)));
		}
		return json;
	}
}
