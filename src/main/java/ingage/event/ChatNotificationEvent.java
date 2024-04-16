package ingage.event;

import java.util.Map;

import com.google.gson.JsonObject;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import ingage.Util;
import ingage.integration.condition.TwitchSubTier;

public class ChatNotificationEvent extends EventBase {
	
	public Type type = EventBase.Type.CHAT_NOTIFICATION;
	
	public String chatter_user_id;
	public String chatter_user_login;
	public String chatter_user_name;
	public boolean chatter_is_anonymous;
	public String color;
	public ChatEvent.Badge[] badges;
	public String system_message;
	public String message_id;
	public ChatEvent.Message message;
	public NoticeType notice_type;
	public Sub sub;
	public Resub resub;
	public SubGift sub_gift;
	public CommunityGiftSub community_sub_gift;
	public GiftPaidUpgrade gift_paid_upgrade;
	public PrimePaidUpgrade prime_paid_upgrade;
	public PayItForward pay_it_forward;
	public Raid raid;
	public Object unraid;//Always either null or empty
	public Announcement announcement;
	public BitsBadgeTier bits_badge_tier;
	public CharityDonation charity_donation;
	
	@Override
	public Type getType() {
		return this.type;
	}
	
	@Override
	public String getUser() {
		if (this.chatter_user_name != null) {
			return this.chatter_user_name;
		}
		return null;
	}
	
	@Override
	public void getVariables(Map<String, Double> variables) {
		if (this.isSub()) {
			variables.put("{CUMULATIVE_MONTHS}", (double) this.getSubCumulativeMonths());
			variables.put("{VALUE}", (double) (this.getSubTier().value));
			variables.put("{TIER}", (double) (this.getSubTier().ordinal()+1));
		}
		if (this.isGiftSubBomb()) {
			variables.put("{VALUE}", (double) (this.getSubBombTier().value * this.getSubBombCount()));
			variables.put("{TIER}", (double) (this.getSubBombTier().ordinal()+1));
		}
	}

	@Override
	public Metadata getMetadata() {
		Metadata meta = new Metadata();
		meta.targetUser = this.getUser();
		meta.user = this.getUser();
		meta.channelName = this.broadcaster_user_name;
		
		if (this.message != null) {
			meta.message = this.message.text;
		}
		return meta;
	}
	
	@Override
	public String getDisplayName() {
		if (this.isGiftSub()) {
			return "Gift Sub";
		}
		if (this.isSub()) {
			return "Subscription";
		}
		if (this.isGiftSubBomb()) {
			return "Sub Bomb";
		}
		return super.getDisplayName();
	}

	@Override
	public void imGui() {
		if (this.message != null && this.message.text != null) {
			ImGui.pushTextWrapPos();
			ImGui.text("Message: "+this.message.text);
			ImGui.pushTextWrapPos();
		}
		if (this.notice_type != null) {
			ImGui.text("Notice Type: "+this.notice_type);
		}
	}
	
	public boolean isSub() {
		return this.sub != null || this.resub != null || this.sub_gift != null;// || this.gift_paid_upgrade != null || this.prime_paid_upgrade != null || this.pay_it_forward != null;
	}
	
	public int getSubCumulativeMonths() {
		//New sub
		if (this.sub != null) {
			return 1;
		}
		if (this.resub != null) {
			return this.resub.cumulative_months;
		}
		//Sub gifts don't have this data
		if (this.sub_gift != null) {
			return 1;
		}
		//Doesn't have the data
		if (this.gift_paid_upgrade != null) {
			return 1;
		}
		//Doesn't have the data
		if (this.prime_paid_upgrade != null) {
			return 1;
		}
		//TODO: Handle pay_it_forward
		return 1;
	}
	
	public int getSubDuration() {
		if (this.sub != null) {
			return this.sub.duration_months;
		}
		if (this.resub != null) {
			return this.resub.duration_months;
		}
		if (this.sub_gift != null) {
			return this.sub_gift.duration_months;
		}
		//Doesn't have the data
		if (this.gift_paid_upgrade != null) {
			return 1;
		}
		//Doesn't have the data
		if (this.prime_paid_upgrade != null) {
			return 1;
		}
		//TODO: Handle pay_it_forward
		return 1;
	}
	
	public boolean isGiftSub() {
		return this.sub_gift != null;
	}
	
	public boolean isPrime() {
		return (this.sub != null && this.sub.is_prime) || (this.resub != null && this.resub.is_prime);
	}
	
	public TwitchSubTier getSubTier() {
		String subTier = "";
		//New sub
		if (this.sub != null) {
			subTier = this.sub.sub_tier;
		}
		if (this.resub != null) {
			subTier = this.resub.sub_tier;
		}
		if (this.sub_gift != null) {
			subTier = this.sub_gift.sub_tier;
		}
		//Doesn't have the data
		if (this.gift_paid_upgrade != null) {
			subTier = "1000";
		}
		//Doesn't have the data
		if (this.prime_paid_upgrade != null) {
			subTier = this.prime_paid_upgrade.sub_tier;
		}
		//TODO: Handle pay_it_forward
		return TwitchSubTier.fromTierString(subTier);
	}
	
	public boolean isPartOfSubBomb() {
		return this.sub_gift != null && this.sub_gift.community_gift_id != null;
	}
	
	public boolean isGiftSubBomb() {
		return this.community_sub_gift != null;
	}
	
	public int getSubBombCount() {
		return this.community_sub_gift.total;
	}
	
	public TwitchSubTier getSubBombTier() {
		return TwitchSubTier.fromTierString(this.community_sub_gift.sub_tier);
	}
	
	public static ChatNotificationEvent fromJson(JsonObject json) {
		ChatNotificationEvent event = Util.GSON.fromJson(json, ChatNotificationEvent.class);
		
		
		
		return event;
	}

	@Override
	public void imGuiForTesting() {
		//Broadcaster username
		ImString broadcasterUsername = new ImString(this.broadcaster_user_name, 1000);
		
		if (ImGui.inputText("Broadcaster Name", broadcasterUsername)) {
			this.broadcaster_user_name = broadcasterUsername.get();
		}
		
		//Chatter username
		ImString chatter = new ImString(this.chatter_user_name != null ? this.chatter_user_name : "", 1000);
		
		if (ImGui.inputText("Chatter Name", chatter)) {
			this.chatter_user_name = chatter.get();
		}
		
		//Sub
		int[] subType = new int[] { this.resub != null ? 1 : this.sub_gift != null ? 2 : this.community_sub_gift != null ? 3 : 0 };
		
		if (ImGui.sliderInt("Notification Type", subType, 0, 3, this.resub != null ? "Sub" : this.sub_gift != null ? "Gift Sub" : this.community_sub_gift != null ? "Sub Bomb" : "None")) {
			if (subType[0] == 0) {
				this.resub = null;
				this.sub_gift = null;
				this.community_sub_gift = null;
			}
			if (subType[0] == 1) {
				this.resub = new Resub();
				this.sub_gift = null;
				this.community_sub_gift = null;
			}
			if (subType[0] == 2) {
				this.resub = null;
				this.sub_gift = new SubGift();
				this.community_sub_gift = null;
			}
			if (subType[0] == 3) {
				this.resub = null;
				this.sub_gift = null;
				this.community_sub_gift = new CommunityGiftSub();
			}
		}
		
		if (this.resub != null) {
			//Tier
			int[] tier = new int[] { this.resub.sub_tier == null ? 1 : TwitchSubTier.fromTierString(this.resub.sub_tier).ordinal() + 1 };
			
			if (ImGui.sliderInt("Tier", tier, 1, 3)) {
				this.resub.sub_tier = TwitchSubTier.values()[tier[0]-1].tierString;
			}
			
			//Cumulative months
			ImInt cumulativeMonths = new ImInt(this.resub.cumulative_months);
			
			if (ImGui.inputInt("Cumulative Months", cumulativeMonths)) {
				this.resub.cumulative_months = cumulativeMonths.get();
			}
			
			//Prime
			if (ImGui.radioButton("Prime", this.resub.is_prime)) {
				this.resub.is_prime =! this.resub.is_prime;
			}
		}
		
		if (this.sub_gift != null) {
			//Recipient
			ImString recipient = new ImString(this.sub_gift.recipient_user_name != null ? this.sub_gift.recipient_user_name : "", 1000);
			
			if (ImGui.inputText("Recipient", recipient)) {
				this.sub_gift.recipient_user_name = recipient.get();
			}
			
			//Tier
			int[] tier = new int[] { this.sub_gift.sub_tier == null ? 1 : TwitchSubTier.fromTierString(this.sub_gift.sub_tier).ordinal() + 1 };
			
			if (ImGui.sliderInt("Tier", tier, 1, 3)) {
				this.sub_gift.sub_tier = TwitchSubTier.values()[tier[0]-1].tierString;
			}
			
			//Duration
			ImInt duration = new ImInt(this.sub_gift.duration_months);
			
			if (ImGui.inputInt("Duration", duration)) {
				this.sub_gift.duration_months = duration.get();
			}
			
			//In sub bomb
			if (ImGui.radioButton("In Sub Bomb", this.sub_gift.community_gift_id != null)) {
				if (this.sub_gift.community_gift_id != null) {
					this.sub_gift.community_gift_id = null;
				} else {
					this.sub_gift.community_gift_id = "";
				}
			}
		}
		
		if (this.community_sub_gift != null) {
			//Tier
			int[] tier = new int[] { this.community_sub_gift.sub_tier == null ? 1 : TwitchSubTier.fromTierString(this.community_sub_gift.sub_tier).ordinal() + 1 };
			
			if (ImGui.sliderInt("Tier", tier, 1, 3)) {
				this.community_sub_gift.sub_tier = TwitchSubTier.values()[tier[0]-1].tierString;
			}
			
			//Count
			ImInt count = new ImInt(this.community_sub_gift.total);
			
			if (ImGui.inputInt("Count", count)) {
				this.community_sub_gift.total = count.get();
			}
		}
	}
	
	public static class Announcement {
		public String color;
	}
	
	public static class Resub {
		public int cumulative_months;
		public int duration_months;
		public String streak_months;
		public String sub_tier;
		public boolean is_prime;
		public boolean is_gift;
		public String gifter_is_anonymous;
		public String gifter_user_id;
		public String gifter_user_name;
		public String gifter_user_login;
	}
	
	public static class SubGift {
		public int duration_months;
		public int cumulative_total;
		public String recipient_user_id;
		public String recipient_user_name;
		public String recipient_user_login;
		public String sub_tier;
		public String community_gift_id;
	}
	
	public static class GiftPaidUpgrade {
		public boolean gifter_is_anonymous;
		public String gifter_user_id;
		public String gifter_user_name;
		public String gifter_user_login;
	}
	
	public static class CommunityGiftSub {
		public String id;
		public int total;
		public int cumulative_total;
		public String sub_tier;
	}
	
	public static class Sub {
		public int duration_months;
		public String sub_tier;
		public boolean is_prime;
	}
	
	public static class Raid {
		public String user_id;
		public String user_name;
		public String user_login;
		public int viewer_count;
		public String profile_image_url;
	}
	
	public static class PrimePaidUpgrade {
		public String sub_tier;
	}
	
	public static class PayItForward {
		public String recipient_user_id;
		public String recipient_user_name;
		public String recipient_user_login;
		public boolean gifter_is_anonymous;
		public String gifter_user_id;
		public String gifter_user_name;
		public String gifter_user_login;
	}
	
	public static class BitsBadgeTier {
		public int tier;
	}
	
	public static class CharityDonation {
		public String charity_name;
		public String amount;
		
		public static class Amount {
			public int value;
			public int decimal_place;
			public String currency;
		}
	}
	
	public static enum NoticeType {
		sub,
		resub,
		sub_gift,
		community_sub_gift,
		gift_paid_upgrade,
		prime_paid_upgrade,
		raid,
		unraid,
		pay_it_forward,
		announcement,
		bits_badge_tier,
		charity_donation;
	}
}
