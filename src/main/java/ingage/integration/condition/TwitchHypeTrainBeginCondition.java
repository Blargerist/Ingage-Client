package ingage.integration.condition;

import ingage.event.EventBase;
import ingage.event.HypeTrainBeginEvent;

public class TwitchHypeTrainBeginCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_HYPE_TRAIN_BEGIN;
	
	@Override
	public ConditionType getType() {
		return this.type;
	}

	@Override
	public boolean test(EventBase event) {
		if (!(event instanceof HypeTrainBeginEvent)) {
			return false;
		}
		HypeTrainBeginEvent e = (HypeTrainBeginEvent)event;
		return true;
	}

	@Override
	public void imGui() {

	}

	@Override
	public ConditionBase clone() {
		TwitchHypeTrainBeginCondition con = new TwitchHypeTrainBeginCondition();
		return con;
	}
}
