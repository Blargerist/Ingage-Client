package ingage.gui;

import imgui.ImGui;
import imgui.type.ImInt;
import ingage.event.EventBase;
import ingage.integration.EventHandler;

public class TestingScreen extends Screen {
	
	public static final TestingScreen INSTANCE = new TestingScreen();
	
	private EventBase event = null;

	@Override
	public void imGui() {
		super.imGui();
		
		ImInt typeIndex = new ImInt(0);
		
		if (ImGui.combo("Event Type", typeIndex, EventBase.Type.getDisplayNames())) {
			EventBase.Type type = EventBase.Type.values()[typeIndex.get()];
			
			if (type != null) {
    			this.event = type.createForTesting();
			}
		}
		
		if (this.event != null) {
			ImGui.newLine();
			this.event.imGuiForTesting();
		}
		
		if (this.event != null) {
			if (ImGui.button("Test")) {
				EventHandler.handleEvent(this.event, true);
			}
		}
	}
}
