package ingage.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import ingage.Util;

public class ChatEvent extends EventBase {
	
	public Type type = EventBase.Type.CHAT;
	
	public String chatter_user_id;
	public String chatter_user_login;
	public String chatter_user_name;
	public String message_id;
	public Message message = new Message();
	public String color;
	public Badge[] badges;
	public String message_type;
	public Cheer cheer = new Cheer();
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
		
		if (this.message != null && this.message.text != null) {
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
		return this.cheer != null && this.cheer.bits > 0;
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

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
		
		//Chatter username
		ImString chatter = new ImString(this.chatter_user_name != null ? this.chatter_user_name : "", 1000);
		
		if (ImGui.inputText("Chatter Name", chatter)) {
			this.chatter_user_name = chatter.get();
		}
		
		//Message
		ImString message = new ImString(this.message.text != null ? this.message.text : "", 1000);
		
		if (ImGui.inputText("Message", message)) {
			this.message.text = message.get();
		}
		
		//Bits
		ImInt bits = new ImInt(this.cheer.bits);
		
		if (ImGui.inputInt("Bits", bits)) {
			this.cheer.bits = bits.get();
		}
		
		//Badges
		boolean moderator = this.badges != null && Arrays.asList(this.badges).stream().anyMatch((badge) -> { return "moderator".equals(badge.set_id); });
		
		if (ImGui.radioButton("Moderator", moderator)) {
			moderator =! moderator;
		}
		boolean subscriber = this.badges != null && Arrays.asList(this.badges).stream().anyMatch((badge) -> { return "subscriber".equals(badge.set_id); });
		
		if (ImGui.radioButton("Subscriber", subscriber)) {
			subscriber =! subscriber;
		}
		boolean staff = this.badges != null && Arrays.asList(this.badges).stream().anyMatch((badge) -> { return "staff".equals(badge.set_id); });
		
		if (ImGui.radioButton("Staff", staff)) {
			staff =! staff;
		}
		boolean broadcaster = this.badges != null && Arrays.asList(this.badges).stream().anyMatch((badge) -> { return "broadcaster".equals(badge.set_id); });
		
		if (ImGui.radioButton("Broadcaster", broadcaster)) {
			broadcaster =! broadcaster;
		}
		
		//Create new badges array
		if (moderator || subscriber || staff || broadcaster) {
			List<Badge> badges = new ArrayList<Badge>();
			
			if (moderator) {
				Badge badge = new Badge();
				badge.set_id = "moderator";
			}
			if (subscriber) {
				Badge badge = new Badge();
				badge.set_id = "subscriber";
			}
			if (staff) {
				Badge badge = new Badge();
				badge.set_id = "staff";
			}
			if (broadcaster) {
				Badge badge = new Badge();
				badge.set_id = "broadcaster";
			}

			this.badges = badges.toArray(new Badge[badges.size()]);
		} else {
			this.badges = null;
		}
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
