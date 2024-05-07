package ingage.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import imgui.ImGui;
import ingage.Logger;
import ingage.connection.IntegrationSocketServer;
import ingage.integration.Integration;
import ingage.integration.IntegrationManager;
import ingage.integration.IntegrationSettings;
import ingage.integration.IntegrationSettingsHandler;

public class IntegrationSettingsScreen extends Screen {

	public static final IntegrationSettingsScreen INSTANCE = new IntegrationSettingsScreen();
	public final List<IntegrationSettings> settings = new ArrayList<IntegrationSettings>();
	
	public void update() {
		this.settings.clear();
		this.settings.addAll(IntegrationSettingsHandler.integrationSettings.stream().map((p) -> { return p.clone(); }).collect(Collectors.toList()));
	}

	@Override
	public void imGui() {
		super.imGui();
		boolean cancel = false;
		
		try {
			if (ImGui.button("Save")) {
				IntegrationSettingsHandler.save(this.settings);
				//Send updated settings
				for (IntegrationSettings settings : this.settings) {
					JsonObject toSend = new JsonObject();
					toSend.addProperty("type", "SETTINGS");
					toSend.add("payload", settings.toJsonPayload());

					IntegrationSocketServer.queueIntegrationMessage(settings.id, toSend);
				}
			}
			ImGui.sameLine();
			
			if (ImGui.button("Cancel")) {
				cancel = true;
			}
			
			for (IntegrationSettings settings : this.settings) {
				Integration integration = IntegrationManager.getIntegration(settings.id);
				
				if (integration != null) {
					if (ImGui.treeNode(settings.uuid, integration.display)) {
						settings.imGui(integration);
						ImGui.treePop();
					}
				}
			}
		} catch(Exception e) {
			Logger.error("Error configuring integration settings", e);
		}
		if (cancel) {
			this.update();
		}
	}
}
