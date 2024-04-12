package ingage.integration.condition;

import imgui.ImGui;
import ingage.event.ChatNotificationEvent;
import ingage.event.EventBase;

public class TwitchSubBombCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_SUB_BOMB;
	public int minTier = 1;
	public int maxTier = 3;
	public int minCount = 1;
	public int maxCount = Short.MAX_VALUE;

	@Override
	public ConditionType getType() {
		return this.type;
	}

	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof ChatNotificationEvent)) {
			return false;
		}
		ChatNotificationEvent e = (ChatNotificationEvent)event;
		
		if (!e.isGiftSubBomb()) {
			return false;
		}
		
		int tier = e.getSubBombTier().ordinal() + 1;
		
		//Test against tier range
		if (tier < this.minTier || tier > this.maxTier) {
			return false;
		}
		
		int count = e.getSubBombCount();
		
		//Test against count range
		if (count < this.minCount || count > this.maxCount) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Tier
		int[] minTier = new int[] {this.minTier};
		
		if (ImGui.sliderInt("Min Tier", minTier, 1, 3)) {
			this.minTier = minTier[0];
		}
		int[] maxTier = new int[] {this.maxTier};
		
		if (ImGui.sliderInt("Max Tier", maxTier, 1, 3)) {
			this.maxTier = maxTier[0];
		}
		
		//Count
		int[] minCount = new int[] {this.minCount};
		
		if (ImGui.sliderInt("Min Count", minCount, 1, Short.MAX_VALUE)) {
			this.minCount = minCount[0];
		}
		int[] maxCount = new int[] {this.maxCount};
		
		if (ImGui.sliderInt("Max Count", maxCount, 1, Short.MAX_VALUE)) {
			this.maxCount = maxCount[0];
		}
	}
	
	@Override
	public ConditionBase clone() {
		TwitchSubBombCondition con = new TwitchSubBombCondition();
		con.minTier = this.minTier;
		con.maxTier = this.maxTier;
		con.minCount = this.minCount;
		con.maxCount = this.maxCount;
		return con;
	}
}
