package ingage.event;

import java.util.Map;

import com.google.gson.JsonObject;

import imgui.ImGui;
import ingage.Util;

public class ChannelPointRedemptionEvent extends EventBase {
	
	public String id;
	public String broadcaster_user_id;
	public String broadcaster_user_login;
	public String broadcaster_user_name;
	public String user_id;
	public String user_login;
	public String user_name;
	public String user_input;
	public String status;
	public Reward reward;
	public String redeemed_at;

	@Override
	public Type getType() {
		return EventBase.Type.CHANNEL_POINT_REDEMPTION;
	}

	@Override
	public void imGui() {
		if (this.reward != null) {
			ImGui.text("ID: "+ this.reward.id);
			ImGui.text("Title: "+ this.reward.title);
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

	public static class Reward {
		public String id;
		public String title;
		public int cost;
		public String prompt;
	}
}
