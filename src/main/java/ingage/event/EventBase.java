package ingage.event;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

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
	public String broadcaster_user_id;
	public String broadcaster_user_login;
	public String broadcaster_user_name = "";
	public abstract Type getType();
	public abstract void imGui();
	public abstract String getUser();
	public abstract void getVariables(Map<String, Double> variables);
	public abstract Metadata getMetadata();
	public abstract void imGuiForTesting();
	
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
			//Gets gson to actually serialize the subclass
			return context.serialize( (Object) src );
		}
	}

	public static enum Type {
		CHAT(ChatEvent.class, "Chat Message", ChatEvent::new),
		CHAT_NOTIFICATION(ChatNotificationEvent.class, "Chat Notification", ChatNotificationEvent::new),
		CHANNEL_POINT_REDEMPTION(ChannelPointRedemptionEvent.class, "Channel Point Redemption", ChannelPointRedemptionEvent::new),
		HYPE_TRAIN_BEGIN(HypeTrainBeginEvent.class, "Hype Train Start", HypeTrainBeginEvent::new),
		HYPE_TRAIN_PROGRESS(HypeTrainProgressEvent.class, "Hype Train Progress", HypeTrainProgressEvent::new),
		HYPE_TRAIN_END(HypeTrainEndEvent.class, "Hype Train End", HypeTrainEndEvent::new),
		STREAMLABS_TIP(StreamlabsTipEvent.class, "Streamlabs Tip", StreamlabsTipEvent::new);
		
		private final Class<? extends EventBase> eventClass;
		private final String displayName;
		private final Supplier<EventBase> createForTesting;
		
		private Type(Class<? extends EventBase> eventClass, String displayName, Supplier<EventBase> createForTesting) {
			this.eventClass = eventClass;
			this.displayName = displayName;
			this.createForTesting = createForTesting;
		}
		
		public Class<? extends EventBase> getEventClass() {
			return this.eventClass;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
		
		public EventBase createForTesting() {
			return this.createForTesting.get();
		}
		
		public static String[] getDisplayNames() {
			return Arrays.asList(Type.values()).stream().map((s) -> s.getDisplayName()).toArray(String[]::new);
		}
	}
}
