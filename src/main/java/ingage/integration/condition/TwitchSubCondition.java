package ingage.integration.condition;

import imgui.ImGui;
import ingage.event.ChatNotificationEvent;
import ingage.event.EventBase;

public class TwitchSubCondition extends ConditionBase {

	public ConditionType type = ConditionType.TWITCH_SUB;
	public int minCumulativeMonths = 0;
	public int maxCumulativeMonths = 600;
	public GiftCondition gift = GiftCondition.EITHER;
	public int minTier = 1;
	public int maxTier = 3;
	public PrimeCondition prime = PrimeCondition.EITHER;
	public int minDuration = 1;
	public int maxDuration = 600;
	public InSubBombCondition inSubBomb = InSubBombCondition.EITHER;

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
		
		if (!e.isSub()) {
			return false;
		}
		
		int cumulativeMonths = e.getSubCumulativeMonths();
		
		//Test against month range
		if (cumulativeMonths < this.minCumulativeMonths || cumulativeMonths > this.maxCumulativeMonths) {
//			Logger.log("Month range failed: "+ cumulativeMonths+" "+this.minCumulativeMonths+"/"+this.maxCumulativeMonths);
			return false;
		}
		boolean isGift = e.isGiftSub();
		
		//Test against gift rule
		if ((isGift && this.gift == GiftCondition.REQUIRE_FALSE) || (!isGift && this.gift == GiftCondition.REQUIRE_TRUE)) {
//			Logger.log("Gift failed: "+ isGift+" "+this.gift);
			return false;
		}
		int tier = e.getSubTier().ordinal() + 1;
		
		//Test against tier range
		if (tier < this.minTier || tier > this.maxTier) {
//			Logger.log("Tier range failed: "+ tier+" "+this.minTier+"/"+this.maxTier);
			return false;
		}
		boolean isPrime = e.isPrime();
		
		//Test against prime rule
		if ((isPrime && this.prime == PrimeCondition.REQUIRE_FALSE) || (!isPrime && this.prime == PrimeCondition.REQUIRE_TRUE)) {
//			Logger.log("Prime failed: "+ isPrime+" "+this.prime);
			return false;
		}
		int duration = Math.max(1, e.getSubDuration());
		
		//Test against month range
		if (duration < this.minDuration || duration > this.maxDuration) {
//			Logger.log("Duration range failed: "+ duration+" "+this.minDuration+"/"+this.maxDuration);
			return false;
		}
		boolean inSubBomb = e.isPartOfSubBomb();
		
		//Test against part of sub bomb rule
		if ((inSubBomb && this.inSubBomb == InSubBombCondition.REQUIRE_FALSE) || (!inSubBomb && this.inSubBomb == InSubBombCondition.REQUIRE_TRUE)) {
			return false;
		}
		return true;
	}

	@Override
	public void imGui() {
		//Cumulative months
		int[] cumulativeMonths = new int[] {this.minCumulativeMonths, this.maxCumulativeMonths};
		
		if (ImGui.sliderInt2("Cumulative Months", cumulativeMonths, 0, 600)) {
			this.minCumulativeMonths = cumulativeMonths[0];
			this.maxCumulativeMonths = cumulativeMonths[1];
		}
		
		//Tier
		int[] tier = new int[] {this.minTier, this.maxTier};
		
		if (ImGui.sliderInt2("Tier", tier, 1, 3)) {
			this.minTier = tier[0];
			this.maxTier = tier[1];
		}
		
		//Duration
		int[] duration = new int[] {this.minDuration, this.maxDuration};
		
		if (ImGui.sliderInt2("Duration", duration, 1, 3)) {
			this.minDuration = duration[0];
			this.maxDuration = duration[1];
		}
		
		//Prime
		int[] prime = new int[] { this.prime.ordinal() };
		
		if (ImGui.sliderInt("Prime", prime, 0, 2, this.prime.getDisplayName())) {
			this.prime = PrimeCondition.values()[prime[0]];
		}
		
		//Gift
		int[] gift = new int[] { this.gift.ordinal() };
		
		if (ImGui.sliderInt("Gift", gift, 0, 2, this.gift.getDisplayName())) {
			this.gift = GiftCondition.values()[gift[0]];
		}
		
		if (this.gift != GiftCondition.REQUIRE_FALSE) {
			//In sub bomb
			int[] inSubBomb = new int[] { this.inSubBomb.ordinal() };
			
			if (ImGui.sliderInt("In Sub Bomb", inSubBomb, 0, 2, this.inSubBomb.getDisplayName())) {
				this.inSubBomb = InSubBombCondition.values()[prime[0]];
			}
		}
	}
	
	@Override
	public ConditionBase clone() {
		TwitchSubCondition con = new TwitchSubCondition();
		con.minCumulativeMonths = this.minCumulativeMonths;
		con.maxCumulativeMonths = this.maxCumulativeMonths;
		con.gift = this.gift;
		con.minTier = this.minTier;
		con.maxTier = this.maxTier;
		con.prime = this.prime;
		con.minDuration = this.minDuration;
		con.maxDuration = this.maxDuration;
		con.inSubBomb = this.inSubBomb;
		return con;
	}

	public static enum GiftCondition {
		REQUIRE_FALSE("Require False"),
		EITHER("Either"),
		REQUIRE_TRUE("Require True");
		
		private String displayName;
		
		private GiftCondition(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
	}

	public static enum PrimeCondition {
		REQUIRE_FALSE("Require False"),
		EITHER("Either"),
		REQUIRE_TRUE("Require True");
		
		private String displayName;
		
		private PrimeCondition(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
	}

	public static enum InSubBombCondition {
		REQUIRE_FALSE("Require False"),
		EITHER("Either"),
		REQUIRE_TRUE("Require True");
		
		private String displayName;
		
		private InSubBombCondition(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
	}
}
