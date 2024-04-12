package ingage.integration.condition;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ingage.Util;
import ingage.event.EventBase;

public abstract class ConditionBase {
	public final transient String uuid = UUID.randomUUID().toString();
	
	public abstract ConditionType getType();
	public abstract boolean test(EventBase event);
	public abstract void imGui();
	public abstract ConditionBase clone();
	
	public static class Deserializer implements JsonDeserializer<ConditionBase>, JsonSerializer<ConditionBase> {

		@Override
		public ConditionBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String typeString = obj.get("type").getAsString();
			ConditionType type = ConditionType.valueOf(typeString);
			
			return Util.GSON.fromJson(json, type.getConditionClass());
		}

		@Override
		public JsonElement serialize(ConditionBase src, Type typeOfSrc, JsonSerializationContext context) {
			//Gets json to actually serialize the subclass
			return context.serialize( (Object) src );
		}
	}
	
	public static enum ConditionType {
		TWITCH_SUB {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchSubCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Sub";
			}

			@Override
			public ConditionBase create() {
				return new TwitchSubCondition();
			}
		},
		TWITCH_SUB_BOMB {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchSubBombCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Sub Bomb";
			}

			@Override
			public ConditionBase create() {
				return new TwitchSubBombCondition();
			}
		},
		TWITCH_BITS {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchBitsCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Bits";
			}

			@Override
			public ConditionBase create() {
				return new TwitchBitsCondition();
			}
		},
		TWITCH_CHANNEL_POINT_REDEMPTION {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchChannelPointRedemptionCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Channel Point Redemption";
			}

			@Override
			public ConditionBase create() {
				return new TwitchChannelPointRedemptionCondition();
			}
		},
		TWITCH_HYPE_TRAIN_BEGIN {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchHypeTrainBeginCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Hype Train Begin";
			}

			@Override
			public ConditionBase create() {
				return new TwitchHypeTrainBeginCondition();
			}
		},
		TWITCH_HYPE_TRAIN_CONTRIBUTION {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchHypeTrainContributionCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Hype Train Contribution";
			}

			@Override
			public ConditionBase create() {
				return new TwitchHypeTrainContributionCondition();
			}
		},
		TWITCH_HYPE_TRAIN_END {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchHypeTrainEndCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Hype Train End";
			}

			@Override
			public ConditionBase create() {
				return new TwitchHypeTrainEndCondition();
			}
		},
		TWITCH_CHAT_MESSAGE {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return TwitchChatMessageCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Twitch Chat Message";
			}

			@Override
			public ConditionBase create() {
				return new TwitchChatMessageCondition();
			}
		},
		STREAMLABS_TIP {
			@Override
			public Class<? extends ConditionBase> getConditionClass() {
				return StreamlabsTipCondition.class;
			}

			@Override
			public String getDisplayName() {
				return "Streamlabs Tip";
			}

			@Override
			public ConditionBase create() {
				return new StreamlabsTipCondition();
			}
		};
		
		public abstract Class<? extends ConditionBase> getConditionClass();
		public abstract String getDisplayName();
		public abstract ConditionBase create();
		
		public static String[] getDisplayNames() {
			return Arrays.asList(ConditionType.values()).stream().map((s) -> s.getDisplayName()).toArray(String[]::new);
		}
	}

	public static enum TrueFalseEitherCondition {
		REQUIRE_FALSE("Require False"),
		EITHER("Either"),
		REQUIRE_TRUE("Require True");
		
		private String displayName;
		
		private TrueFalseEitherCondition(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
	}
}
