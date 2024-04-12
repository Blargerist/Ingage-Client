package ingage.integration.condition;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.event.ChannelPointRedemptionEvent;
import ingage.event.EventBase;

public class TwitchChannelPointRedemptionCondition extends ConditionBase {
	
	public ConditionType type = ConditionType.TWITCH_CHANNEL_POINT_REDEMPTION;
	public String redemptionID;
	public String title;

	@Override
	public ConditionType getType() {
		return this.type;
	}

	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof ChannelPointRedemptionEvent)) {
			return false;
		}
		ChannelPointRedemptionEvent e = (ChannelPointRedemptionEvent)event;
		
		//Test against redemption id
		if (this.redemptionID != null && !this.redemptionID.isEmpty() && !this.redemptionID.equals(e.reward.id)) {
			return false;
		}
		//Test against title
		if (this.title != null && !this.title.isEmpty() && !this.title.equals(e.reward.title)) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		ImString redemptionID = new ImString(this.redemptionID, 300);
		
		if (ImGui.inputText("ID", redemptionID)) {
			this.redemptionID = redemptionID.get();
		}
		
		ImString title = new ImString(this.title, 300);
		
		if (ImGui.inputText("Title", title)) {
			this.title = title.get();
		}
	}
	
	@Override
	public ConditionBase clone() {
		TwitchChannelPointRedemptionCondition con = new TwitchChannelPointRedemptionCondition();
		con.redemptionID = this.redemptionID;
		con.title = this.title;
		return con;
	}
}
