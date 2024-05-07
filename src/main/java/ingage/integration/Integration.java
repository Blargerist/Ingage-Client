package ingage.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ingage.integration.effect.EffectBase;
import ingage.integration.effect.parameter.ParameterBase;

public class Integration {

	public final String id ;
	public final String display;
	public final List<EffectBase> effects = new ArrayList<EffectBase>();
	public List<ParameterBase<?, ?>> settings = new ArrayList<ParameterBase<?, ?>>();
	//For gui
	public transient String[] effectDisplayNames = new String[] {};
	
	public Integration() {
		this.id = null;
		this.display = null;
	}
	
	public Integration(String id, String display) {
		this.id = id;
		this.display = display;
	}
	
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
