package ingage.integration.effect;

import com.google.gson.JsonObject;

public class EffectMessage {
	public final String integrationID;
	public final JsonObject toSend;
	
	public EffectMessage(String integrationID, JsonObject toSend) {
		this.integrationID = integrationID;
		this.toSend = toSend;
	}
}
