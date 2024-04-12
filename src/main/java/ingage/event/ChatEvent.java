package ingage.event;

import java.util.Map;

import com.google.gson.JsonObject;

import imgui.ImGui;
import ingage.Util;

public class ChatEvent extends EventBase {
	
	public Type type = EventBase.Type.CHAT;
	
	public String broadcaster_user_id;
	public String broadcaster_user_login;
	public String broadcaster_user_name;
	public String chatter_user_id;
	public String chatter_user_login;
	public String chatter_user_name;
	public String message_id;
	public Message message;
	public String color;
	public Badge[] badges;
	public String message_type;
	public Cheer cheer;
	public Reply reply;
	public String channel_points_custom_reward_id;
	
	@Override
	public Type getType() {
		return this.type;
	}
	
	@Override
	public String getUser() {
		if (this.chatter_user_name != null) {
			return this.chatter_user_name;
		}
		return null;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		if (this.cheer != null) {
			variables.put("{BITS}", (double) this.cheer.bits);
			variables.put("{VALUE}", (double) this.cheer.bits);
		}
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.targetUser = this.getUser();
		meta.user = this.getUser();
		meta.channelName = this.broadcaster_user_name;
		
		if (this.message != null) {
			meta.message = this.message.text;
		}
		return meta;
	}
	
	@Override
	public String getDisplayName() {
		if (this.isBits()) {
			return "Bits";
		}
		return super.getDisplayName();
	}

	@Override
	public void imGui() {
		if (this.message != null && this.message.text != null) {
			ImGui.pushTextWrapPos();
			ImGui.text("Message: "+this.message.text);
			ImGui.pushTextWrapPos();
		}
		if (this.badges != null && this.badges.length > 0) {
			if (ImGui.treeNode("Badges")) {
				for (int i = 0; i < this.badges.length; i++) {
					String setIDText = "Set ID: "+this.badges[i].set_id;
					String idText = "ID: "+this.badges[i].id;
					String infoText = "Info: "+this.badges[i].info;
					
					if (ImGui.beginListBox("##Badge"+i, 200, ImGui.getFrameHeight() * 2.8F)) {
						ImGui.text(setIDText);
						ImGui.text(idText);
						ImGui.text(infoText);
						ImGui.endListBox();
					}
				}
				ImGui.treePop();
			}
		}
	}
	
	public boolean isBits() {
		return this.cheer != null;
	}
	
	public int getBits() {
		return this.cheer.bits;
	}
	
	public String getText() {
		if (this.message != null && this.message.text != null) {
			return this.message.text;
		}
		return null;
	}
	
	public boolean moderator() {
		if (this.badges != null && this.badges.length > 0) {
			for (int i = 0; i < this.badges.length; i++) {
				if ("moderator".equals(this.badges[i].set_id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean subscriber() {
		if (this.badges != null && this.badges.length > 0) {
			for (int i = 0; i < this.badges.length; i++) {
				if ("subscriber".equals(this.badges[i].set_id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean staff() {
		if (this.badges != null && this.badges.length > 0) {
			for (int i = 0; i < this.badges.length; i++) {
				if ("staff".equals(this.badges[i].set_id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean broadcaster() {
		if (this.badges != null && this.badges.length > 0) {
			for (int i = 0; i < this.badges.length; i++) {
				if ("broadcaster".equals(this.badges[i].set_id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ChatEvent fromJson(JsonObject json) {
		ChatEvent event = Util.GSON.fromJson(json, ChatEvent.class);
		
		
		
		return event;
	}
	
	public static class Message {
		public String text;
		public Fragment[] fragments;
		
		public static class Fragment {
			public String type;
			public String text;
			public Cheermote cheermote;
			public Emote emote;
			public Mention mention;
			
			public static class Mention {
				public String user_id;
				public String user_login;
				public String user_name;
			}
			
			public static class Cheermote {
				public String prefix;
				public int bits;
				public int tier;
			}
		}
	}
	
	public static class Badge {
		public String set_id;
		public String id;
		public String info;
	}
	
	public static class Emote {
		public String id;
		public String emote_set_id;
		public String owner_id;
		public String[] format;
	}
	
	public static class Reply {
		public String parent_message_id;
		public String parent_message_body;
		public String parent_user_id;
		public String parent_user_name;
		public String parent_user_login;
		public String thread_message_id;
		public String thread_user_id;
		public String thread_user_name;
		public String thread_user_login;
	}
	
	public static class Cheer {
		public int bits;
	}
}
