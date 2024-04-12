package ingage.event;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ingage.Util;

public abstract class EventBase {

	public transient String uuid = UUID.randomUUID().toString();
	public Instant time = Instant.now();
	public abstract Type getType();
	public abstract void imGui();
	public abstract String getUser();
	public abstract void getVariables(Map<String, Double> variables);
	public abstract Metadata getMetadata();
	
	public String getDisplayName() {
		return this.getType().getDisplayName();
	}
	
	public static class Deserializer implements JsonDeserializer<EventBase>, JsonSerializer<EventBase> {
		@Override
		public EventBase deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String typeString = obj.get("type").getAsString();
			Type type = EventBase.Type.valueOf(typeString);
			
			return Util.GSON.fromJson(json, type.getEventClass());
		}

		@Override
		public JsonElement serialize(EventBase src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			//Gets json to actually serialize the subclass
			return context.serialize( (Object) src );
		}
	}

	public static enum Type {
		CHAT {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return ChatEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Chat Message";
			}
		},
		CHAT_NOTIFICATION {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return ChatNotificationEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Chat Notification";
			}
		},
		CHANNEL_POINT_REDEMPTION {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return ChannelPointRedemptionEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Channel Point Redemption";
			}
		},
		HYPE_TRAIN_BEGIN {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return HypeTrainBeginEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Hype Train Start";
			}
		},
		HYPE_TRAIN_PROGRESS {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return HypeTrainProgressEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Hype Train Progress";
			}
		},
		HYPE_TRAIN_END {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return HypeTrainEndEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Hype Train End";
			}
		},
		STREAMLABS_TIP {
			@Override
			public Class<? extends EventBase> getEventClass() {
				return StreamlabsTipEvent.class;
			}

			@Override
			public String getDisplayName() {
				return "Streamlabs Tip";
			}
		};
		
		public abstract Class<? extends EventBase> getEventClass();
		public abstract String getDisplayName();
		
		public static String[] getDisplayNames() {
			return Arrays.asList(Type.values()).stream().map((s) -> s.getDisplayName()).toArray(String[]::new);
		}
	}
}
