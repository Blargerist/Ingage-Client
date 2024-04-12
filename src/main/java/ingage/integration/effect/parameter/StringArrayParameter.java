package ingage.integration.effect.parameter;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImString;

public class StringArrayParameter extends ParameterBase<String[], String[]> {
	@Override
	public String[] getDefault() {
		return defaultValue;
	}

	@Override
	public void imGui() {
		
	}

	@Override
	public ParameterConfigBase<String[], String[]> createConfig() {
		Config config = new Config();
		config.value = this.defaultValue != null ? this.defaultValue : new String[] {};
		config.parameter = this;
		config.parameterID = this.id;
		config.type = this.type;
		return config;
	}
	
	public static class Config extends ParameterBase.ParameterConfigBase<String[], String[]> {
		@Override
		public String[] getValue() {
			return value;
		}

		@Override
		public void setValue(String[] value) {
			this.value = value;
		}

		@Override
		public String[] evaluate(Map<String, Double> variables) {
			return this.value;
		}

		@Override
		public void imGui() {
			StringBuilder valueBuilder = new StringBuilder();
			
			if (this.value.length > 0) {
				valueBuilder.append(this.value[0]);
				
				for (int i = 1; i < this.value.length; i++) {
					valueBuilder.append("\n");
					valueBuilder.append(this.value[i]);
				}
			}
			
			String valueString = valueBuilder.toString();
			ImString value = new ImString(valueString, valueString.length() + 1000);
			
			if (ImGui.inputTextMultiline(this.parameter.display, value, 300, (ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY()) * Math.max(2, this.value.length + 1))) {
				this.value = value.get().trim().split("\n");
			}
		}

		@Override
		public ParameterBase.ParameterConfigBase<String[], String[]> clone() {
			Config config = new Config();
			config.parameter = this.parameter;
			config.parameterID = this.parameterID;
			config.type = this.type;
			config.value = this.value;
			return config;
		}
	}
}
