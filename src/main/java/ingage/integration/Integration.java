package ingage.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ingage.integration.effect.EffectBase;

public class Integration {

	public final String id = null;
	public final String display = null;
	public final List<EffectBase> effects = new ArrayList<EffectBase>();
	//For gui
	public transient String[] effectDisplayNames = new String[] {};
	
	public EffectBase getEffect(String id) {
		for (EffectBase effect : this.effects) {
			if (effect.id.equals(id)) {
				return effect;
			}
		}
		return null;
	}
	
	public void buildDisplayNamesArray() {
		effectDisplayNames = effects.stream().map((e) -> {
			return e.display;
		}).collect(Collectors.toList()).toArray(effectDisplayNames);
	}
	
	public void imGui() {
		
	}
}
