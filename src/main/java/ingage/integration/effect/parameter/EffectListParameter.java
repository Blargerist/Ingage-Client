package ingage.integration.effect.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import imgui.ImGui;
import imgui.type.ImInt;
import ingage.integration.Event.ImGuiAddEffect;
import ingage.integration.effect.EffectBase.EffectConfig;

public class EffectListParameter extends ParameterBase<List<EffectConfig>, List<EffectConfig>> {

	@Override
	public List<EffectConfig> getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<List<EffectConfig>, List<EffectConfig>> createConfig() {
		Config config = new Config();
		config.value = this.defaultValue != null ? this.defaultValue : new ArrayList<EffectConfig>();
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class Config extends ParameterBase.ParameterConfigBase<List<EffectConfig>, List<EffectConfig>> {
		private transient ImGuiAddEffect addEffect = null;
	    private transient List<EffectConfig> effectsToRemove = new ArrayList<EffectConfig>();
	    private transient List<EffectConfig> effectsToAdd = new ArrayList<EffectConfig>();
	    private transient List<EffectConfig> effectsToMoveUp = new ArrayList<EffectConfig>();
	    private transient List<EffectConfig> effectsToMoveDown = new ArrayList<EffectConfig>();
		
		@Override
		public List<EffectConfig> getValue() {
			return value;
		}

		@Override
		public void setValue(List<EffectConfig> value) {
			this.value = value;
		}

		@Override
		public List<EffectConfig> evaluate(Map<String, Double> variables) {
			return this.value;
		}

		@Override
		public void imGui(EffectConfig config) {
			boolean isWeighted = false;
			
			//Find the weighted parameter and get its value
			if (config.effectID.equals("list") && config.integrationID.equals("ingage")) {
				for (ParameterConfigBase<?, ?> p : config.parameters) {
					if (p.type == ParameterType.BOOLEAN && p.parameterID.equals("weighted")) {
						isWeighted = ((BooleanParameter.Config)p).value;
					}
				}
			}
			
			if (ImGui.treeNode("Effects")) {
	        	if (ImGui.beginPopup("Add Effect")) {
	        		if (addEffect == null) {
	        			addEffect = new ImGuiAddEffect();
	        		}
	        		if (addEffect.imGui()) {
	        			this.effectsToAdd.add(addEffect.effect);
	        			addEffect = null;
	        		}
	    			ImGui.endPopup();
	    		}
	        	
	            if (ImGui.button("Add Effect")) {
	    			ImGui.openPopup("Add Effect");
	            }
				
				for (EffectConfig effect : this.value) {
					if (ImGui.treeNode(effect.uuid, effect.effectDisplay != null ? effect.effectDisplay : effect.effectID)) {
						if (ImGui.button("Remove")) {
							this.effectsToRemove.add(effect);
						}
						//If list is weighted, show the weight value
						if (isWeighted) {
							ImInt weight = new ImInt(effect.weight);
							
							if (ImGui.inputInt("Weight", weight)) {
								effect.weight = weight.get();
							}
						}
						effect.imGui();
						ImGui.treePop();
					}
				}
				ImGui.treePop();
			}
			
			//Move effects up
			for (EffectConfig effect : effectsToMoveUp) {
				int index = value.indexOf(effect);
				Collections.swap(value, value.indexOf(effect), Math.max(0, index - 1));
			}
			effectsToMoveUp.clear();
			
			//Move effects down
			for (EffectConfig effect : effectsToMoveDown) {
				int index = value.indexOf(effect);
				Collections.swap(value, value.indexOf(effect), Math.min(value.size() - 1, index + 1));
			}
			effectsToMoveDown.clear();
			
			//Remove deleted effects
			for (EffectConfig effect : effectsToRemove) {
				value.remove(effect);
			}
			effectsToRemove.clear();
			
			//Add new effects
			value.addAll(effectsToAdd);
			effectsToAdd.clear();
		}

		@Override
		public ParameterBase.ParameterConfigBase<List<EffectConfig>, List<EffectConfig>> clone() {
			Config config = new Config();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			
			List<EffectConfig> effects = new ArrayList<EffectConfig>();
			
			for (EffectConfig e : this.value) {
				effects.add(e.clone());
			}
			config.value = effects;
			return config;
		}
	}
}
