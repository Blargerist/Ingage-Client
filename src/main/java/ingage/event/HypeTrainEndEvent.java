package ingage.event;

import java.util.Map;

import imgui.ImGui;
import ingage.event.HypeTrainBeginEvent.Contributor;

public class HypeTrainEndEvent extends EventBase {
	
	public String id;
	public String broadcaster_user_id;
	public String broadcaster_user_login;
	public String broadcaster_user_name;
	public int level;
	public int total;
	public Contributor[] top_contributions;
	public String started_at;
	public String ended_at;
	public String cooldown_ends_at;

	@Override
	public Type getType() {
		return Type.HYPE_TRAIN_END;
	}

	@Override
	public void imGui() {
		if (this.id != null) {
			ImGui.text("ID: "+this.id);
		}
		ImGui.text("Level: "+this.level);
		ImGui.text("Total: "+this.total);
	}

	@Override
	public String getUser() {
		if (this.broadcaster_user_name != null) {
			return this.broadcaster_user_name;
		}
		return null;
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.user = "Hype Train";
		meta.channelName = this.broadcaster_user_name;
		return meta;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		if (this.top_contributions != null) {
			int total = 0;
			
			for (int i = 0; i < this.top_contributions.length; i++) {
				total += this.top_contributions[i].total;
			}
			variables.put("{VALUE}", (double) (total));
		}
	}
}
