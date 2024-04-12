package ingage.integration.condition;

import imgui.ImGui;
import ingage.event.EventBase;
import ingage.event.StreamlabsTipEvent;

public class StreamlabsTipCondition extends ConditionBase {

	public ConditionType type = ConditionType.STREAMLABS_TIP;
	public float minAmount = 0;
	public float maxAmount = 1000;

	@Override
	public ConditionType getType() {
		return this.type;
	}
	
	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof StreamlabsTipEvent)) {
			return false;
		}
		StreamlabsTipEvent e = (StreamlabsTipEvent)event;
		
		float amount = (float) e.amount;
		
		//Test against amount range
		if (amount < this.minAmount || amount > this.maxAmount) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Amount
		float[] amount = new float[] {this.minAmount, this.maxAmount};
		
		if (ImGui.dragFloat2("Max Amount", amount, 1, 0)) {
			this.minAmount = amount[0];
			this.maxAmount = amount[1];
		}
	}
	
	@Override
	public ConditionBase clone() {
		StreamlabsTipCondition con = new StreamlabsTipCondition();
		con.minAmount = this.minAmount;
		con.maxAmount = this.maxAmount;
		return con;
	}
}
