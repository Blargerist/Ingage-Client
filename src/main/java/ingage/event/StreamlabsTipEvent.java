package ingage.event;

import java.util.Map;

import imgui.ImGui;

public class StreamlabsTipEvent extends EventBase {
	
	public Type type = EventBase.Type.STREAMLABS_TIP;
	
	public String user;
	public double amount;
	public String message;
	public String broadcaster_user_name;
	
	public StreamlabsTipEvent() {
		
	}
	
	public StreamlabsTipEvent(String user, double amount, String message, String broadcaster_user_name) {
		this.user = user;
		this.amount = amount;
		this.message = message;
		this.broadcaster_user_name = broadcaster_user_name;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public void imGui() {
		ImGui.text("Broadcaster: "+this.broadcaster_user_name);
		ImGui.text("Amount: "+this.amount);
		
		if (this.message != null) {
			ImGui.pushTextWrapPos();
			ImGui.text("Message: "+this.message);
			ImGui.pushTextWrapPos();
		}
	}

	@Override
	public String getUser() {
		return this.user;
	}

	@Override
	public void getVariables(Map<String, Double> variables) {
		variables.put("{VALUE}", this.amount);
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.targetUser = this.getUser();
		meta.user = this.getUser();
		meta.channelName = this.broadcaster_user_name;
		
		if (this.message != null) {
			meta.message = this.message;
		}
		return meta;
	}

}
