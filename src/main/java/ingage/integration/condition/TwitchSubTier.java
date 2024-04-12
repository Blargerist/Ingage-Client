package ingage.integration.condition;

public enum TwitchSubTier {
	ONE("1000", 500),
	TWO("2000", 1000),
	THREE("3000", 2500);
	
	public final String tierString;
	public final int value;
	
	private TwitchSubTier(String tierString, int value) {
		this.tierString = tierString;
		this.value = value;
	}
	
	public static TwitchSubTier fromTierString(String tierString) {
		for (int i = 0; i < TwitchSubTier.values().length; i++) {
			TwitchSubTier tier = TwitchSubTier.values()[i];
			
			if (tier.tierString.equals(tierString)) {
				return tier;
			}
		}
		//Default to 1
		return TwitchSubTier.ONE;
	}
}
