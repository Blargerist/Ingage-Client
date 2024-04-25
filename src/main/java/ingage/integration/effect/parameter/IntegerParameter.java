package ingage.integration.effect.parameter;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.integration.effect.EffectBase.EffectConfig;
import io.netty.util.internal.StringUtil;
import net.objecthunter.exp4j.ExpressionBuilder;

public class IntegerParameter extends ParameterBase<String, Integer> {
	@Override
	public String getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<String, Integer> createConfig() {
		IntegerParameterConfig config = new IntegerParameterConfig();
		config.value = this.defaultValue != null ? this.defaultValue : "";
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class IntegerParameterConfig extends ParameterBase.ParameterConfigBase<String, Integer> {
		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public Integer evaluate(Map<String, Double> variables) {
			return (int) new ExpressionBuilder(this.value).variables(variables.keySet()).build().setVariables(variables).evaluate();
		}

		@Override
		public void imGui(EffectConfig config) {
			if (this.parameter != null) {
				ImString value = new ImString(this.value, 300);
				
				if (ImGui.inputText(this.parameter.display, value)) {
					this.value = value.get();
				}
				if (!StringUtil.isNullOrEmpty(this.parameter.description)) {
					if (ImGui.isItemHovered()) {
						ImGui.setTooltip(this.parameter.description);
					}
				}
			}
		}

		@Override
		public ParameterBase.ParameterConfigBase<String, Integer> clone() {
			IntegerParameterConfig config = new IntegerParameterConfig();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			config.value = this.value;
			return config;
		}
	}
}
