package ingage.event;

import java.util.Map;

import com.google.gson.JsonObject;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.Util;

public class ChannelPointRedemptionEvent extends EventBase {
	
	public String id;
	public String user_id;
	public String user_login;
	public String user_name;
	public String user_input;
	public String status;
	public Reward reward = new Reward();
	public String redeemed_at;

	@Override
	public Type getType() {
		return EventBase.Type.CHANNEL_POINT_REDEMPTION;
	}

	@Override
	public void imGui() {
		if (this.reward != null) {
			if (this.reward.id != null) {
				ImGui.text("ID: "+ this.reward.id);
			}
			if (this.reward.title != null) {
				ImGui.text("Title: "+ this.reward.title);
			}
			ImGui.text("Cost: "+ this.reward.cost);
		}
		if (this.user_input != null) {
			ImGui.pushTextWrapPos();
			ImGui.text("Message: "+this.user_input);
			ImGui.pushTextWrapPos();
		}
	}

	@Override
	public String getUser() {
		if (this.user_name != null) {
			return this.user_name;
		}
		return null;
	}
	
	public int getCost() {
		return this.reward.cost;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.targetUser = this.getUser();
		meta.user = this.getUser();
		meta.channelName = this.broadcaster_user_name;
		meta.message = this.user_input;
		return meta;
	}
	
	public static ChannelPointRedemptionEvent fromJson(JsonObject json) {
		ChannelPointRedemptionEvent event = Util.GSON.fromJson(json, ChannelPointRedemptionEvent.class);
		return event;
	}

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
		
		//Chatter username
		ImString chatter = new ImString(this.user_name != null ? this.user_name : "", 1000);
		
		if (ImGui.inputText("Chatter Name", chatter)) {
			this.user_name = chatter.get();
		}
		
		//Reward title
		ImString rewardTitle = new ImString(this.reward.title != null ? this.reward.title : "", 1000);
		
		if (ImGui.inputText("Reward Title", rewardTitle)) {
			this.reward.title = rewardTitle.get();
		}
		
		//Reward ID
		ImString rewardID = new ImString(this.reward.id != null ? this.reward.id : "", 1000);
		
		if (ImGui.inputText("Reward ID", rewardID)) {
			this.reward.id = rewardID.get();
		}
		
		//Message
		ImString message = new ImString(this.user_input != null ? this.user_input : "", 1000);
		
		if (ImGui.inputText("Message", message)) {
			this.user_input = message.get();
		}
	}

	public static class Reward {
		public String id;
		public String title;
		public int cost;
		public String prompt;
	}
}
