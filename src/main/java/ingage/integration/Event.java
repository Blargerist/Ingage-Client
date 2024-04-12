package ingage.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import ingage.Logger;
import ingage.event.EventBase;
import ingage.event.Metadata;
import ingage.integration.condition.ConditionBase;
import ingage.integration.condition.ConditionBase.ConditionType;
import ingage.integration.effect.EffectBase;
import ingage.integration.effect.EffectBase.EffectConfig;
import ingage.integration.effect.EffectMessage;

public class Event {
	
	public String name = "Event";
	public final transient String uuid = UUID.randomUUID().toString();
	public final List<ConditionBase> conditions = new ArrayList<ConditionBase>();
	public final List<EffectConfig> effects = new ArrayList<EffectConfig>();
	
	private transient ImGuiAddEffect addEffect = null;
    private transient List<EffectConfig> effectsToRemove = new ArrayList<EffectConfig>();
    private transient List<EffectConfig> effectsToAdd = new ArrayList<EffectConfig>();
    private transient List<EffectConfig> effectsToMoveUp = new ArrayList<EffectConfig>();
    private transient List<EffectConfig> effectsToMoveDown = new ArrayList<EffectConfig>();
	
    private transient List<ConditionBase> conditionsToRemove = new ArrayList<ConditionBase>();
    private transient List<ConditionBase> conditionsToAdd = new ArrayList<ConditionBase>();
    private transient List<ConditionBase> conditionsToMoveUp = new ArrayList<ConditionBase>();
    private transient List<ConditionBase> conditionsToMoveDown = new ArrayList<ConditionBase>();
    
    public boolean test(EventBase connectionEvent) {
    	for (ConditionBase condition : this.conditions) {
    		if (!condition.test(connectionEvent)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public List<EffectMessage> getEffectMessages(EventBase connectionEvent) {
    	Map<String, Double> variables = new HashMap<String, Double>();
    	connectionEvent.getVariables(variables);
    	
    	List<EffectMessage> list = new ArrayList<EffectMessage>();
    	
    	for (EffectConfig effect : this.effects) {
    		try {
        		list.add(effect.toEffectMessage(variables));
    		} catch (Exception e) {
    			Logger.error("Exception evaluating effect "+effect.effectID+" in event "+this.name, e);
    		}
    	}
    	return list;
    }
	
	public void imGui() {

		ImString name = new ImString(this.name, 200);
		
		if (ImGui.inputText("Name", name)) {
			this.name = name.get();
		}
		
		if (ImGui.treeNode("Conditions")) {
			if (ImGui.beginPopup("Add Condition")) {
				ImInt index = new ImInt(0);
				
				if (ImGui.combo("Type", index, ConditionType.getDisplayNames())) {
					ConditionType type = ConditionType.values()[index.get()];
					
					if (type != null) {
	        			this.conditionsToAdd.add(type.create());
						ImGui.closeCurrentPopup();
					}
				}
    			ImGui.endPopup();
    		}
        	
            if (ImGui.button("Add Condition")) {
    			ImGui.openPopup("Add Condition");
            }
			for (ConditionBase condition : this.conditions) {
				
				if (ImGui.treeNode(condition.uuid, condition.getType().getDisplayName())) {
					if (ImGui.button("Remove")) {
						this.conditionsToRemove.add(condition);
					}
					condition.imGui();
					ImGui.treePop();
				}
			}
			ImGui.treePop();
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
			
			for (EffectConfig effect : this.effects) {
				
				if (ImGui.treeNode(effect.uuid, effect.effectDisplay != null ? effect.effectDisplay : effect.effectID)) {
					if (ImGui.button("Remove")) {
						this.effectsToRemove.add(effect);
					}
					effect.imGui();
					ImGui.treePop();
				}
			}
			ImGui.treePop();
		}
		
		//Move effects up
		for (EffectConfig effect : effectsToMoveUp) {
			int index = effects.indexOf(effect);
			Collections.swap(effects, effects.indexOf(effect), Math.max(0, index - 1));
		}
		effectsToMoveUp.clear();
		
		//Move effects down
		for (EffectConfig effect : effectsToMoveDown) {
			int index = effects.indexOf(effect);
			Collections.swap(effects, effects.indexOf(effect), Math.min(effects.size() - 1, index + 1));
		}
		effectsToMoveDown.clear();
		
		//Remove deleted effects
		for (EffectConfig effect : effectsToRemove) {
			effects.remove(effect);
		}
		effectsToRemove.clear();
		
		//Add new effects
		effects.addAll(effectsToAdd);
		effectsToAdd.clear();
		
		//Move conditions up
		for (ConditionBase condition : conditionsToMoveUp) {
			int index = conditions.indexOf(condition);
			Collections.swap(conditions, conditions.indexOf(condition), Math.max(0, index - 1));
		}
		conditionsToMoveUp.clear();
		
		//Move conditions down
		for (ConditionBase condition : conditionsToMoveDown) {
			int index = conditions.indexOf(condition);
			Collections.swap(conditions, conditions.indexOf(condition), Math.min(conditions.size() - 1, index + 1));
		}
		conditionsToMoveDown.clear();
		
		//Remove deleted conditions
		for (ConditionBase condition : conditionsToRemove) {
			conditions.remove(condition);
		}
		conditionsToRemove.clear();
		
		//Add new conditions
		conditions.addAll(conditionsToAdd);
		conditionsToAdd.clear();
	}
	
	public Event clone() {
		Event event = new Event();
		event.name = this.name;
		
		for (ConditionBase condition : this.conditions) {
			event.conditions.add(condition.clone());
		}
		for (EffectConfig effect : this.effects) {
			event.effects.add(effect.clone());
		}
		return event;
	}
	
	private static class ImGuiAddEffect {
		private int integrationIndex;
		private EffectConfig effect = null;
		
		public boolean imGui() {
			boolean selected = false;
			ImInt integrationIndex = new ImInt(this.integrationIndex);
    		
			if (ImGui.combo("Integration", integrationIndex, IntegrationManager.integrationDisplayNames)) {
				this.integrationIndex = integrationIndex.get();
			}
			Integration integration = IntegrationManager.integrations.get(this.integrationIndex);
			
			if (integration != null) {
				ImInt effectIndex = new ImInt(0);
				
				if (ImGui.combo("Effect", effectIndex, integration.effectDisplayNames)) {
					EffectBase effect = integration.effects.get(effectIndex.get());
					
					if (effect != null) {
						this.effect = effect.createEffectConfig();
						ImGui.closeCurrentPopup();
						selected = true;
					}
				}
			}
			return selected;
		}
	}
}
