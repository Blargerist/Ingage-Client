package ingage.integration.effect.parameter;

import ingage.integration.effect.parameter.DoubleParameter.DoubleParameterConfig;
import ingage.integration.effect.parameter.IntegerParameter.IntegerParameterConfig;
import ingage.integration.effect.parameter.ParameterBase.ParameterConfigBase;
import ingage.integration.effect.parameter.StringParameter.StringParameterConfig;

public enum ParameterType {
	STRING {
		@Override
		public Class<? extends ParameterBase<?, ?>> getParameterClass() {
			return StringParameter.class;
		}

		@Override
		public Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass() {
			return StringParameterConfig.class;
		}
	},
	DECIMAL {
		@Override
		public Class<? extends ParameterBase<?, ?>> getParameterClass() {
			return DoubleParameter.class;
		}

		@Override
		public Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass() {
			return DoubleParameterConfig.class;
		}
	},
	INTEGER {
		@Override
		public Class<? extends ParameterBase<?, ?>> getParameterClass() {
			return IntegerParameter.class;
		}

		@Override
		public Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass() {
			return IntegerParameterConfig.class;
		}
	},
	STRING_ARRAY {
		@Override
		public Class<? extends ParameterBase<?, ?>> getParameterClass() {
			return StringArrayParameter.class;
		}

		@Override
		public Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass() {
			return StringArrayParameter.Config.class;
		}
	},
	BOOLEAN {
		@Override
		public Class<? extends ParameterBase<?, ?>> getParameterClass() {
			return BooleanParameter.class;
		}

		@Override
		public Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass() {
			return BooleanParameter.Config.class;
		}
	};
	
	public abstract Class<? extends ParameterBase<?, ?>> getParameterClass();
	public abstract Class<? extends ParameterConfigBase<?, ?>> getParameterConfigClass();
}
