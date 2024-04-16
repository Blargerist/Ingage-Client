package ingage.event;

import java.util.Arrays;
import java.util.Map;

import imgui.ImGui;
import imgui.type.ImString;

public class HypeTrainBeginEvent extends EventBase {
	
	public String id;
	public int total;
	public int goal;
	public Contributor[] top_contributions;
	public Contributor last_contribution;
	public int level;
	public String started_at;
	public String expires_at;

	@Override
	public Type getType() {
		return Type.HYPE_TRAIN_BEGIN;
	}

	@Override
	public void imGui() {
		if (this.id != null) {
			ImGui.text("ID: "+this.id);
		}
	}

	@Override
	public String getUser() {
		if (this.broadcaster_user_name != null) {
			return this.broadcaster_user_name;
		}
		return null;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.user = "Hype Train";
		meta.channelName = this.broadcaster_user_name;
		return meta;
	}

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
	}
	
	public static class Contributor {
		public String user_id;
		public String user_login;
		public String user_name;
		public Type type;
		public int total;
		
		public static enum Type {
			bits("Bits"),
			subscription("Sub"),
			other("Other");
			
			private String displayName;
			
			private Type(String displayName) {
				this.displayName = displayName;
			}
			
			public String getDisplayName() {
				return this.displayName;
			}
			
			public static String[] getDisplayNames() {
				return Arrays.asList(Type.values()).stream().map((s) -> s.getDisplayName()).toArray(String[]::new);
			}
		}
	}
}
