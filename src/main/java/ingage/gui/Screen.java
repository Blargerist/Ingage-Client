package ingage.gui;

import imgui.ImGui;
import ingage.Window;

public abstract class Screen {

	public void imGui() {
		if (ImGui.button("Auth")) {
			Window.screen = UsersScreen.INSTANCE;
		}
		ImGui.sameLine();
		
		if (ImGui.button("Events")) {
			Window.screen = IntegrationEventsScreen.INSTANCE;
		}
		ImGui.sameLine();
		
		if (ImGui.button("History")) {
			Window.screen = HistoryScreen.INSTANCE;
		}
		ImGui.sameLine();
		
		if (ImGui.button("Testing")) {
			Window.screen = TestingScreen.INSTANCE;
		}
		
		ImGui.separator();
	}
}
