package ingage.integration.condition;

import imgui.ImGui;
import ingage.event.ChatEvent;
import ingage.event.EventBase;

public class TwitchBitsCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_BITS;
	public int minBits = 0;
	public int maxBits = Integer.MAX_VALUE / 2;

	@Override
	public ConditionType getType() {
		return this.type;
	}
	
	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof ChatEvent)) {
			return false;
		}
		ChatEvent e = (ChatEvent)event;
		
		if (!e.isBits()) {
			return false;
		}
		
		int bits = e.getBits();
		
		//Test against bits range
		if (bits < this.minBits || bits > this.maxBits) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Bits
		int[] bits = new int[] {this.minBits, this.maxBits};
		
		if (ImGui.dragInt2("Bits", bits, 100, 0)) {
			this.minBits = bits[0];
			this.maxBits = bits[1];
		}
	}
	
	@Override
	public ConditionBase clone() {
		TwitchBitsCondition con = new TwitchBitsCondition();
		con.minBits = this.minBits;
		con.maxBits = this.maxBits;
		return con;
	}
}
