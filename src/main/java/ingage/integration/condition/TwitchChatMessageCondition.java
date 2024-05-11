package ingage.integration.condition;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.event.ChatEvent;
import ingage.event.EventBase;

public class TwitchChatMessageCondition extends ConditionBase {
	
	public ConditionType type = ConditionType.TWITCH_CHAT_MESSAGE;
	public String exactMessage = "";
	public boolean exactMessageIgnoreCase = true;
	public String startsWith = "";
	public boolean startsWithIgnoreCase = true;
	public String endsWith = "";
	public boolean endsWithIgnoreCase = true;
	public String contains = "";
	public boolean containsIgnoreCase = true;
	public TrueFalseEitherCondition subscriber = TrueFalseEitherCondition.EITHER;
	public TrueFalseEitherCondition moderator = TrueFalseEitherCondition.EITHER;
	public TrueFalseEitherCondition staff = TrueFalseEitherCondition.EITHER;
	public TrueFalseEitherCondition broadcaster = TrueFalseEitherCondition.EITHER;
	public String[] userWhitelist = new String[] {};

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
		
		String text = e.getText();
		
		//If we want an exact message
		if (this.exactMessage != null && !this.exactMessage.isEmpty()) {
			//If there is no message
			if (text == null) {
				return false;
			}
			//If ignoring case and doesn't match
			if (this.exactMessageIgnoreCase && !this.exactMessage.equalsIgnoreCase(text)) {
				return false;
			}
			//If not ignoring case and doesn't match
			if (!this.exactMessageIgnoreCase && !this.exactMessage.equals(text)) {
				return false;
			}
		}
		//If we want to start with text
		if (this.startsWith != null && !this.startsWith.isEmpty()) {
			//If there is no message
			if (text == null) {
				return false;
			}
			//If ignoring case and doesn't match
			if (this.startsWithIgnoreCase && !this.startsWith.equalsIgnoreCase(text)) {
				return false;
			}
			//If not ignoring case and doesn't match
			if (!this.startsWithIgnoreCase && !this.startsWith.equals(text)) {
				return false;
			}
		}
		//If we want to end with text
		if (this.endsWith != null && !this.endsWith.isEmpty()) {
			//If there is no message
			if (text == null) {
				return false;
			}
			//If ignoring case and doesn't match
			if (this.endsWithIgnoreCase && !this.endsWith.equalsIgnoreCase(text)) {
				return false;
			}
			//If not ignoring case and doesn't match
			if (!this.endsWithIgnoreCase && !this.endsWith.equals(text)) {
				return false;
			}
		}
		//If we want to contain text
		if (this.contains != null && !this.contains.isEmpty()) {
			//If there is no message
			if (text == null) {
				return false;
			}
			//If ignoring case and doesn't match
			if (this.containsIgnoreCase && !this.contains.equalsIgnoreCase(text)) {
				return false;
			}
			//If not ignoring case and doesn't match
			if (!this.containsIgnoreCase && !this.contains.equals(text)) {
				return false;
			}
		}
		boolean subscriber = e.subscriber();
		
		//Test against subscriber rule
		if ((subscriber && this.subscriber == TrueFalseEitherCondition.REQUIRE_FALSE) || (!subscriber && this.subscriber == TrueFalseEitherCondition.REQUIRE_TRUE)) {
			return false;
		}
		boolean moderator = e.moderator();
		
		//Test against subscriber rule
		if ((moderator && this.moderator == TrueFalseEitherCondition.REQUIRE_FALSE) || (!moderator && this.moderator == TrueFalseEitherCondition.REQUIRE_TRUE)) {
			return false;
		}
		boolean staff = e.staff();
		
		//Test against subscriber rule
		if ((staff && this.staff == TrueFalseEitherCondition.REQUIRE_FALSE) || (!staff && this.staff == TrueFalseEitherCondition.REQUIRE_TRUE)) {
			return false;
		}
		boolean broadcaster = e.broadcaster();
		
		//Test against subscriber rule
		if ((broadcaster && this.broadcaster == TrueFalseEitherCondition.REQUIRE_FALSE) || (!broadcaster && this.broadcaster == TrueFalseEitherCondition.REQUIRE_TRUE)) {
			return false;
		}
		String user = e.getUser();
		
		//Test against user whitelist
		if (this.userWhitelist != null && this.userWhitelist.length > 0) {
			boolean allWhitespace = true;
			boolean userMatches = false;
			
			for (int i = 0; i < this.userWhitelist.length; i++) {
				if (!this.userWhitelist[i].isEmpty()) {
					allWhitespace = false;
				}
				if (this.userWhitelist[i].equalsIgnoreCase(user)) {
					userMatches = true;
					break;
				}
			}
			
			if (!userMatches && !allWhitespace) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void imGui() {
		//Message
		ImString message = new ImString(this.exactMessage, 300);
		
		if (ImGui.inputText("Message", message)) {
			this.exactMessage = message.get();
		}
		if (this.exactMessage != null && !this.exactMessage.isEmpty()) {
			if (ImGui.radioButton("Ignore Case###Message", this.exactMessageIgnoreCase)) {
				this.exactMessageIgnoreCase =! this.exactMessageIgnoreCase;
			}
		}
		//Starts With
		ImString startsWith = new ImString(this.startsWith, 300);
		
		if (ImGui.inputText("Starts With", startsWith)) {
			this.startsWith = startsWith.get();
		}
		if (this.startsWith != null && !this.startsWith.isEmpty()) {
			if (ImGui.radioButton("Ignore Case###Starts With", this.startsWithIgnoreCase)) {
				this.startsWithIgnoreCase =! this.startsWithIgnoreCase;
			}
		}
		//Ends With
		ImString endsWith = new ImString(this.endsWith, 300);
		
		if (ImGui.inputText("Ends With", endsWith)) {
			this.endsWith = endsWith.get();
		}
		if (this.endsWith != null && !this.endsWith.isEmpty()) {
			if (ImGui.radioButton("Ignore Case###Ends With", this.endsWithIgnoreCase)) {
				this.endsWithIgnoreCase =! this.endsWithIgnoreCase;
			}
		}
		//Contains
		ImString contains = new ImString(this.contains, 300);
		
		if (ImGui.inputText("Contains", contains)) {
			this.contains = contains.get();
		}
		if (this.contains != null && !this.contains.isEmpty()) {
			if (ImGui.radioButton("Ignore Case###Contains", this.containsIgnoreCase)) {
				this.containsIgnoreCase =! this.containsIgnoreCase;
			}
		}
		
		//Subscriber
		int[] subscriber = new int[] { this.subscriber.ordinal() };
		
		if (ImGui.sliderInt("Subscriber", subscriber, 0, 2, this.subscriber.getDisplayName())) {
			this.subscriber = TrueFalseEitherCondition.values()[subscriber[0]];
		}
		//Moderator
		int[] moderator = new int[] { this.moderator.ordinal() };
		
		if (ImGui.sliderInt("Moderator", moderator, 0, 2, this.moderator.getDisplayName())) {
			this.moderator = TrueFalseEitherCondition.values()[moderator[0]];
		}
		//Staff
		int[] staff = new int[] { this.staff.ordinal() };
		
		if (ImGui.sliderInt("Staff", staff, 0, 2, this.staff.getDisplayName())) {
			this.staff = TrueFalseEitherCondition.values()[staff[0]];
		}
		//Broadcaster
		int[] broadcaster = new int[] { this.broadcaster.ordinal() };
		
		if (ImGui.sliderInt("Broadcaster", broadcaster, 0, 2, this.broadcaster.getDisplayName())) {
			this.broadcaster = TrueFalseEitherCondition.values()[broadcaster[0]];
		}
		
		StringBuilder whitelistStringBuilder = new StringBuilder();
		
		if (this.userWhitelist.length > 0) {
			whitelistStringBuilder.append(this.userWhitelist[0]);
			
			for (int i = 1; i < this.userWhitelist.length; i++) {
				whitelistStringBuilder.append("\n");
				whitelistStringBuilder.append(this.userWhitelist[i]);
			}
		}
		String whitelistString = whitelistStringBuilder.toString();
		
		ImString whitelist = new ImString(whitelistString, whitelistString.length() + 100);
		
		//User whitelist
		if (ImGui.inputTextMultiline("Whitelist", whitelist, 300, (ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY()) * Math.max(2, this.userWhitelist.length + 1))) {
			this.userWhitelist = whitelist.get().split("\n");
		}
	}

	@Override
	public ConditionBase clone() {
		TwitchChatMessageCondition con = new TwitchChatMessageCondition();
		con.exactMessage = this.exactMessage;
		con.exactMessageIgnoreCase = this.exactMessageIgnoreCase;
		con.startsWith = this.startsWith;
		con.startsWithIgnoreCase = this.startsWithIgnoreCase;
		con.endsWith = this.endsWith;
		con.endsWithIgnoreCase = this.endsWithIgnoreCase;
		con.contains = this.contains;
		con.containsIgnoreCase = this.containsIgnoreCase;
		con.subscriber = this.subscriber;
		con.moderator = this.moderator;
		con.staff = this.staff;
		con.broadcaster = this.broadcaster;
		con.userWhitelist = this.userWhitelist;
		return con;
	}
}
