package ingage.event;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import ingage.event.HypeTrainBeginEvent.Contributor;

public class HypeTrainProgressEvent extends EventBase {
	
	public String id;
	public int level;
	public int total;
	public int progress;
	public int goal;
	public Contributor[] top_contributions;
	public Contributor last_contribution;
	public String started_at;
	public String expires_at;
	
	public HypeTrainProgressEvent() {
		this.last_contribution = new Contributor();
		this.last_contribution.type = HypeTrainBeginEvent.Contributor.Type.other;
	}

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

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
		
		//Contribution type
		ImInt typeIndex = new ImInt(this.last_contribution != null && this.last_contribution.type != null ? this.last_contribution.type.ordinal() : 0);

		if (ImGui.combo("Contribution Type", typeIndex, HypeTrainBeginEvent.Contributor.Type.getDisplayNames())) {
			if (this.last_contribution == null) {
				this.last_contribution = new Contributor();
			}
			this.last_contribution.type = HypeTrainBeginEvent.Contributor.Type.values()[typeIndex.get()];
		}
		
		//Contribution points
		ImInt contributionPoints = new ImInt(this.last_contribution != null ? this.last_contribution.total : 0);
		
		if (ImGui.inputInt("Contribution Points", contributionPoints)) {
			if (this.last_contribution == null) {
				this.last_contribution = new Contributor();
			}
			this.last_contribution.total = contributionPoints.get();
		}
		
		//Level
		ImInt level = new ImInt(this.level);
		
		if (ImGui.inputInt("Level", level)) {
			this.level = level.get();
		}
		
		//Total points
		ImInt totalPoints = new ImInt(this.total);
		
		if (ImGui.inputInt("Total Points", totalPoints)) {
			this.total = totalPoints.get();
		}
	}
}
