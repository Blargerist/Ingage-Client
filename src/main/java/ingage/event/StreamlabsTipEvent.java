package ingage.event;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImDouble;
import imgui.type.ImString;

public class StreamlabsTipEvent extends EventBase {
	
	public Type type = EventBase.Type.STREAMLABS_TIP;
	
	public String user;
	public double amount;
	public String message;
	
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

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
		
		//Amount
		ImDouble amount = new ImDouble(this.amount);
		
		if (ImGui.inputDouble("Amount", amount)) {
			this.amount = amount.get();
		}
	}
}
