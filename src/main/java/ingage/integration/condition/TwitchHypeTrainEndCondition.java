package ingage.integration.condition;

import imgui.ImGui;
import ingage.event.EventBase;
import ingage.event.HypeTrainEndEvent;

public class TwitchHypeTrainEndCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_HYPE_TRAIN_END;
	public int minLevel;
	public int maxLevel;
	public int minTotalPoints;
	public int maxTotalPoints;

	@Override
	public ConditionType getType() {
		return this.type;
	}

	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof HypeTrainEndEvent)) {
			return false;
		}
		HypeTrainEndEvent e = (HypeTrainEndEvent)event;
		
		int level = e.level;
		
		//Test against level range
		if (level < this.minLevel || level > this.maxLevel) {
			return false;
		}
		
		int totalPoints = e.total;
		
		//Test against total contributions range
		if (totalPoints < this.minTotalPoints || totalPoints > this.maxTotalPoints) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Level
		int[] level = new int[] {this.minLevel, this.maxLevel};
		
		if (ImGui.sliderInt2("Level", level, 0, 500)) {
			this.minLevel = level[0];
			this.maxLevel = level[1];
		}
		//Total Points
		int[] totalPoints = new int[] {this.minTotalPoints, this.maxTotalPoints};
		
		if (ImGui.sliderInt2("Total Points", totalPoints, 0, Integer.MAX_VALUE / 2)) {
			this.minTotalPoints = totalPoints[0];
			this.maxTotalPoints = totalPoints[1];
		}
	}

	@Override
	public ConditionBase clone() {
		TwitchHypeTrainEndCondition con = new TwitchHypeTrainEndCondition();
		con.minLevel = this.minLevel;
		con.maxLevel = this.maxLevel;
		con.minTotalPoints = this.minTotalPoints;
		con.maxTotalPoints = this.maxTotalPoints;
		return con;
	}
}
