package ingage.integration.effect.parameter;

import java.util.Map;

import imgui.ImGui;
import ingage.integration.effect.EffectBase.EffectConfig;
import io.netty.util.internal.StringUtil;

public class BooleanParameter extends ParameterBase<Boolean, Boolean> {
	@Override
	public Boolean getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<Boolean, Boolean> createConfig() {
		Config config = new Config();
		config.value = this.defaultValue != null ? this.defaultValue : false;
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class Config extends ParameterBase.ParameterConfigBase<Boolean, Boolean> {
		@Override
		public Boolean getValue() {
			return value;
		}

		@Override
		public void setValue(Boolean value) {
			this.value = value;
		}

		@Override
		public Boolean evaluate(Map<String, Double> variables) {
			return Boolean.valueOf(this.value);
		}

		@Override
		public void imGui(EffectConfig config) {
			if (this.parameter != null) {
				if (ImGui.radioButton(this.parameter.display, this.value)) {
					this.value =! this.value;
				}
				if (!StringUtil.isNullOrEmpty(this.parameter.description)) {
					if (ImGui.isItemHovered()) {
						ImGui.setTooltip(this.parameter.description);
					}
				}
			}
		}

		@Override
		public ParameterBase.ParameterConfigBase<Boolean, Boolean> clone() {
			Config config = new Config();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			config.value = this.value;
			return config;
		}
	}
}
