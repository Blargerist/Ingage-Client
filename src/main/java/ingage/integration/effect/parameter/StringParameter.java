package ingage.integration.effect.parameter;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.integration.effect.EffectBase.EffectConfig;

public class StringParameter extends ParameterBase<String, String> {
	@Override
	public String getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<String, String> createConfig() {
		StringParameterConfig config = new StringParameterConfig();
		config.value = this.defaultValue != null ? this.defaultValue : "";
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class StringParameterConfig extends ParameterBase.ParameterConfigBase<String, String> {
		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String evaluate(Map<String, Double> variables) {
			return this.value;
		}

		@Override
		public void imGui(EffectConfig config) {
			if (this.parameter != null) {
				ImString value = new ImString(this.value, 300);
				
				if (ImGui.inputText(this.parameter.display, value)) {
					this.value = value.get();
				}
			}
		}

		@Override
		public ParameterBase.ParameterConfigBase<String, String> clone() {
			StringParameterConfig config = new StringParameterConfig();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			config.value = this.value;
			return config;
		}
	}
}
