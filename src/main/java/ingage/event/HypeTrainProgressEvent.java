package ingage.event;

import java.util.Map;

import imgui.ImGui;
import ingage.event.HypeTrainBeginEvent.Contributor;

public class HypeTrainProgressEvent extends EventBase {
	
	public String id;
	public String broadcaster_user_id;
	public String broadcaster_user_login;
	public String broadcaster_user_name;
	public int level;
	public int total;
	public int progress;
	public int goal;
	public Contributor[] top_contributions;
	public Contributor last_contribution;
	public String started_at;
	public String expires_at;

	@Override
	public Type getType() {
		return Type.HYPE_TRAIN_PROGRESS;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		if (this.last_contribution != null) {
			variables.put("{VALUE}", (double) (this.last_contribution.total));
		}
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.targetUser = this.getUser();
		meta.user = this.getUser();
		meta.channelName = this.broadcaster_user_name;
		return meta;
	}

	@Override
	public void imGui() {
		if (this.id != null) {
			ImGui.text("ID: "+this.id);
		}
		ImGui.text("Level: "+this.level);
		ImGui.text("Total: "+this.total);
		ImGui.text("Progress: "+this.progress);
		ImGui.text("Goal: "+this.goal);
		
		if (this.last_contribution != null) {
			ImGui.text("Contribution: "+this.last_contribution.total);
			ImGui.text("Contribution Type: "+this.last_contribution.type);
			
			if (this.last_contribution.user_name != null) {
				ImGui.text("Contributor: "+this.last_contribution.user_name);
			}
		}
	}
	
	public int getContribution() {
		if (this.last_contribution != null) {
			return this.last_contribution.total;
		}
		return 0;
	}
	
	public Contributor.Type getContributionType() {
		return this.last_contribution.type;
	}

	@Override
	public String getUser() {
		if (this.last_contribution != null) {
			return this.last_contribution.user_name;
		}
		return null;
	}
}
