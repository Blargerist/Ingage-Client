package ingage.event;

import java.util.Map;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import ingage.event.HypeTrainBeginEvent.Contributor;

public class HypeTrainEndEvent extends EventBase {
	
	public String id;
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

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
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
