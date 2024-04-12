package ingage.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import ingage.event.ChatEvent;
import ingage.event.ChatNotificationEvent;
import ingage.event.EventBase;
import ingage.event.HistoryManager;
import ingage.integration.EventHandler;

public class HistoryScreen extends Screen {
	
	public static final HistoryScreen INSTANCE = new HistoryScreen();

	private final List<EventBase> filteredEventCopy = new ArrayList<EventBase>();
	private final List<EventBase> shortEventCopy = new ArrayList<EventBase>();
	private final List<EventBase> eventCopy = new ArrayList<EventBase>();
	
	private boolean showChatMessages = true;
	private boolean showChatNotifications = true;
	private boolean showChannelPointRedemptions = true;
	private boolean showBits = true;
	private boolean showStreamlabsTips = true;
	private boolean showSubBombs = true;
	private boolean showSubs = true;
	private boolean showGiftsubs = true;
	
	public EventBase replay = null;

	@Override
	public void imGui() {
		super.imGui();
		
		if (ImGui.radioButton("Chat Messages", showChatMessages)) {
			this.showChatMessages =! this.showChatMessages;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Chat Notifications", showChatNotifications)) {
			this.showChatNotifications =! this.showChatNotifications;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Channel Point Redemptions", showChannelPointRedemptions)) {
			this.showChannelPointRedemptions =! this.showChannelPointRedemptions;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Bits", showBits)) {
			this.showBits =! this.showBits;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Subs", showSubs)) {
			this.showSubs =! this.showSubs;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Gift Subs", showGiftsubs)) {
			this.showGiftsubs =! this.showGiftsubs;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Sub Bombs", showSubBombs)) {
			this.showSubBombs =! this.showSubBombs;
		}
		ImGui.sameLine();
		
		if (ImGui.radioButton("Streamlabs Tips", showStreamlabsTips)) {
			this.showStreamlabsTips =! this.showStreamlabsTips;
		}
		//Copy list in case it's modified while we're rendering;
		eventCopy.clear();
		HistoryManager.cloneConnectionEventHistory(eventCopy);
		//Filter events
		filteredEventCopy.clear();
		filteredEventCopy.addAll(eventCopy.stream()
				.filter((e) -> {
					//Bits
					if (e.getType() == EventBase.Type.CHAT && ((ChatEvent)e).isBits()) {
						return this.showBits;
					}
					//Non bit chat messages
					if (e.getType() == EventBase.Type.CHAT) {
						return this.showChatMessages;
					}
					//Sub bombs
					if (e.getType() == EventBase.Type.CHAT_NOTIFICATION && ((ChatNotificationEvent)e).isGiftSubBomb()) {
						return this.showSubBombs;
					}
					//Gift subs
					if (e.getType() == EventBase.Type.CHAT_NOTIFICATION && ((ChatNotificationEvent)e).isGiftSub()) {
						return this.showGiftsubs;
					}
					//Subs
					if (e.getType() == EventBase.Type.CHAT_NOTIFICATION && ((ChatNotificationEvent)e).isSub()) {
						return this.showSubs;
					}
					//Non-sub related chat notifications
					if (e.getType() == EventBase.Type.CHAT_NOTIFICATION) {
						return this.showChatNotifications;
					}
					//Channel point redemptions
					if (e.getType() == EventBase.Type.CHANNEL_POINT_REDEMPTION) {
						return this.showChannelPointRedemptions;
					}
					//Streamlabs tips
					if (e.getType() == EventBase.Type.STREAMLABS_TIP) {
						return this.showStreamlabsTips;
					}
					//Show any without filters
					return true;
				})
				//Sort by time
				.sorted((e, e2) -> {
					return e.time.compareTo(e2.time);
				})
				.collect(Collectors.toList()));
		//Get the most recent 100
		shortEventCopy.clear();
		shortEventCopy.addAll(filteredEventCopy.subList(filteredEventCopy.size()-Math.min(filteredEventCopy.size(), 100), filteredEventCopy.size()));

		ImGui.separator();
		
		ImGui.pushStyleColor(ImGuiCol.FrameBg, 0, 0, 0, 0);
		
		if (ImGui.beginListBox("##events", ImGui.getColumnWidth(), ImGui.getWindowHeight() - ImGui.getFrameHeightWithSpacing() * 3)) {
			ImGui.popStyleColor();
			//Reverse order
			for (int i = this.shortEventCopy.size() - 1; i >= 0; i--) {
				EventBase event = this.shortEventCopy.get(i);
				String treeDisplay = event.getDisplayName();
				String user = event.getUser();
				
				if (user != null) {
					treeDisplay = treeDisplay+" - "+user;
				}
				if (ImGui.treeNode(event.uuid, treeDisplay)) {
					if (ImGui.button("Replay")) {
						this.replay = event;
					}
					event.imGui();
					ImGui.treePop();
				}
			}
			ImGui.endListBox();
		} else {
			ImGui.popStyleColor();
		}
		
		if (this.replay != null) {
			EventHandler.handleEvent(replay, true);
			this.replay = null;
		}
	}

}
