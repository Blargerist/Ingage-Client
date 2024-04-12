package ingage.integration.effect.parameter;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImString;
import net.objecthunter.exp4j.ExpressionBuilder;

public class DoubleParameter extends ParameterBase<String, Double> {
	@Override
	public String getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<String, Double> createConfig() {
		DoubleParameterConfig config = new DoubleParameterConfig();
		config.value = this.defaultValue != null ? this.defaultValue : "";
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class DoubleParameterConfig extends ParameterBase.ParameterConfigBase<String, Double> {
		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public Double evaluate(Map<String, Double> variables) {
			return new ExpressionBuilder(this.value).variables(variables.keySet()).build().setVariables(variables).evaluate();
		}

		@Override
		public void imGui() {
			if (this.parameter != null) {
				ImString value = new ImString(this.value, 300);
				
				if (ImGui.inputText(this.parameter.display, value)) {
					this.value = value.get();
				}
			}
		}

		@Override
		public ParameterBase.ParameterConfigBase<String, Double> clone() {
			DoubleParameterConfig config = new DoubleParameterConfig();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			config.value = this.value;
			return config;
		}
	}
}
