package ingage.integration.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ingage.Logger;
import ingage.Util;
import ingage.integration.Event;
import ingage.integration.Integration;
import ingage.integration.IntegrationManager;
import ingage.integration.effect.parameter.BooleanParameter;
import ingage.integration.effect.parameter.EffectListParameter;
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
	
	public static class EffectConfig {
		public final transient String uuid = UUID.randomUUID().toString();
		public transient Integration integration;
		public transient EffectBase effect;
		public String integrationID;
		public String effectID;
		public String effectDisplay;
		public int weight = 100;
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
		
		public List<EffectMessage> toEffectMessages(Event event, Map<String, Double> variables) {
			List<EffectMessage> messages = new ArrayList<EffectMessage>();
			
			//Special handling for internal integration
			if (this.integrationID.equals("ingage") && this.effectID.equals("list")) {
				boolean weighted = false;
				int listLength = 0;
				
				for (ParameterConfigBase<?, ?> parameter : this.parameters) {
					if (parameter.parameterID.equals("weighted")) {//Check if it's weighted
						weighted = ((BooleanParameter.Config)parameter).value;
					} else if (parameter.parameterID.equals("effects")) {//Get size of the list
						listLength = ((EffectListParameter.Config)parameter).value.size();
					}
				}
				//Make sure it's weighted and there's at least two options to pick from
				if (weighted && listLength > 1) {
					int index = 0;
					
					for (ParameterConfigBase<?, ?> parameter : this.parameters) {
						if (parameter.parameterID.equals("effects")) {
							//Get a random index
							index = Util.weightedRandomIndex(((EffectListParameter.Config)parameter).value, Util.random);
							//Add messages only for the chosen index
							messages.addAll(((EffectListParameter.Config)parameter).value.get(index).toEffectMessages(event, variables));
						}
					}
				} else {
					for (ParameterConfigBase<?, ?> parameter : this.parameters) {
						if (parameter.parameterID.equals("effects")) {
							try {
								//Add all the messages
								messages.addAll(handleEffectList(event, variables, ((EffectListParameter.Config)parameter).value));
				    		} catch (Exception e) {
				    			Logger.error("Exception evaluating effect "+this.effectID+" in event "+event.name, e);
				    		}
						}
					}
				}
			} else {
				//Normal effect
				JsonObject json = new JsonObject();
				json.addProperty("integration", this.integrationID);
				json.addProperty("type", this.effectID);
				
				JsonObject parameters = new JsonObject();
				json.add("values", parameters);
				
				for (ParameterConfigBase<?, ?> parameter : this.parameters) {
					parameters.add(parameter.parameterID, Util.GSON.fromJson(Util.GSON.toJson(parameter.evaluate(variables)), JsonElement.class));
				}
				messages.add(new EffectMessage(this.integrationID, json));
			}
			return messages;
		}
		
		private List<EffectMessage> handleEffectList(Event event, Map<String, Double> variables, List<EffectBase.EffectConfig> list) {
			List<EffectMessage> messages = new ArrayList<EffectMessage>();
			
			for (EffectConfig effect : list) {
				//Special handling for internal integration
				if (effect.integrationID.equals("ingage") && effect.effectID.equals("list")) {
					boolean weighted = false;
					int listLength = 0;
					
					for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
						if (parameter.parameterID.equals("weighted")) {//Check if it's weighted
							weighted = ((BooleanParameter.Config)parameter).value;
						} else if (parameter.parameterID.equals("effects")) {//Get size of the list
							listLength = ((EffectListParameter.Config)parameter).value.size();
						}
					}
					//Make sure it's weighted and there's at least two options to pick from
					if (weighted && listLength > 1) {
						int index = 0;
						
						for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
							if (parameter.parameterID.equals("effects")) {
								//Get a random index
								index = Util.weightedRandomIndex(((EffectListParameter.Config)parameter).value, Util.random);
								//Add messages only for the chosen index
								messages.addAll(((EffectListParameter.Config)parameter).value.get(index).toEffectMessages(event, variables));
							}
						}
					} else {
						for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
							if (parameter.parameterID.equals("effects")) {
								try {
									//Add all the messages
									messages.addAll(handleEffectList(event, variables, ((EffectListParameter.Config)parameter).value));
					    		} catch (Exception e) {
					    			Logger.error("Exception evaluating effect "+effect.effectID+" in event "+event.name, e);
					    		}
							}
						}
					}
				} else {
					//Normal effect
					JsonObject json = new JsonObject();
					json.addProperty("integration", effect.integrationID);
					json.addProperty("type", effect.effectID);
					
					JsonObject parameters = new JsonObject();
					json.add("values", parameters);
					
					for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
						parameters.add(parameter.parameterID, Util.GSON.fromJson(Util.GSON.toJson(parameter.evaluate(variables)), JsonElement.class));
					}
					messages.add(new EffectMessage(effect.integrationID, json));
				}
			}
			return messages;
		}
		
		public void imGui() {
			for (ParameterConfigBase<?, ?> parameter : this.parameters) {
				parameter.imGui(this);
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
