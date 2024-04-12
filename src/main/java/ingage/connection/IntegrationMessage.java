package ingage.connection;

import com.google.gson.JsonObject;

public class IntegrationMessage {
	public final String integrationID;
	public final JsonObject toSend;
	
	public IntegrationMessage(String integrationID, JsonObject toSend) {
		this.integrationID = integrationID;
		this.toSend = toSend;
	}
}
