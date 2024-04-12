package ingage.integration.condition;

import java.util.Arrays;

import imgui.ImGui;
import imgui.type.ImInt;
import ingage.event.EventBase;
import ingage.event.HypeTrainBeginEvent;
import ingage.event.HypeTrainProgressEvent;

public class TwitchHypeTrainContributionCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_HYPE_TRAIN_CONTRIBUTION;
	public int minContribution = 0;
	public int maxContribution = Integer.MAX_VALUE/2;
	public int minLevel = 0;
	public int maxLevel = 500;
	public int minTotalPoints = 0;
	public int maxTotalPoints = Integer.MAX_VALUE/2;
	public Type contributionType = Type.ANY;

	@Override
	public ConditionType getType() {
		return this.type;
	}

	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof HypeTrainProgressEvent)) {
			return false;
		}
		HypeTrainProgressEvent e = (HypeTrainProgressEvent)event;
		
		int contribution = e.getContribution();
		
		//Test against contribution range
		if (contribution < this.minContribution || contribution > this.maxContribution) {
			return false;
		}
		
		int level = e.level;
		
		//Test against level range
		if (level < this.minLevel || level > this.maxLevel) {
			return false;
		}
		
		int totalContributions = e.total;
		
		//Test against total contributions range
		if (totalContributions < this.minTotalPoints || totalContributions > this.maxTotalPoints) {
			return false;
		}
		
		HypeTrainBeginEvent.Contributor.Type contributionType = e.getContributionType();
		
		//Test against contribution type
		switch (contributionType) {
			case bits: 
				if (this.contributionType != Type.ANY && this.contributionType != Type.BITS) {
					return false;
				}
				break;
			case subscription:
				if (this.contributionType != Type.ANY && this.contributionType != Type.SUB) {
					return false;
				}
				break;
			case other:
				if (this.contributionType != Type.ANY && this.contributionType != Type.OTHER) {
					return false;
				}
				break;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Contribution
		int[] contribution = new int[] {this.minContribution, this.maxContribution};
		
		if (ImGui.sliderInt2("Contribution", contribution, 0, Integer.MAX_VALUE / 2)) {
			this.minContribution = contribution[0];
			this.maxContribution = contribution[1];
		}
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
		
		//Contribution type
		ImInt typeIndex = new ImInt(this.contributionType.ordinal());
		
		if (ImGui.combo("Contribution Type", typeIndex, Type.getDisplayNames())) {
			this.contributionType = Type.values()[typeIndex.get()];
		}
	}

	@Override
	public ConditionBase clone() {
		TwitchHypeTrainContributionCondition con = new TwitchHypeTrainContributionCondition();
		con.minContribution = this.minContribution;
		con.maxContribution = this.maxContribution;
		con.minLevel = this.minLevel;
		con.maxLevel = this.maxLevel;
		con.minTotalPoints = this.minTotalPoints;
		con.maxTotalPoints = this.maxTotalPoints;
		con.contributionType = this.contributionType;
		return con;
	}
	
	public static enum Type {
		ANY("Any"),
		SUB("Sub"),
		BITS("Bits"),
		OTHER("Other");
		
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
