package ingage.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ingage.Logger;
import ingage.Util;
import ingage.gui.IntegrationSettingsScreen;
import ingage.integration.effect.parameter.ParameterBase;
import ingage.integration.effect.parameter.ParameterBase.ParameterConfigBase;

public class IntegrationSettingsHandler {
	
	public static final File INTEGRATION_SETTINGS_FOLDER = new File("./data/integrations/settings/");
	public static final List<IntegrationSettings> integrationSettings = new ArrayList<IntegrationSettings>();
	
	public static IntegrationSettings getSettings(String id) {
		for (IntegrationSettings settings : integrationSettings) {
			if (settings.id.equals(id)) {
				return settings;
			}
		}
		return null;
	}
	
	public static void addSettings(Integration integration) {
		if (integration.settings.size() > 0) {
			boolean found = false;
			
			for (IntegrationSettings settings : IntegrationSettingsHandler.integrationSettings) {
				if (integration.id.equals(settings.id)) {
					found = true;
					//Update in case there are any changes
					updateSettings(settings, integration);
					break;
				}
			}
			if (!found) {
				IntegrationSettings settings = new IntegrationSettings();
				settings.id = integration.id;
				updateSettings(settings, integration);
				IntegrationSettingsHandler.integrationSettings.add(settings);
			}
		}
		save(integrationSettings);
	}
	
	public static void load() {
		INTEGRATION_SETTINGS_FOLDER.mkdirs();
		
		//Clear currently loaded settings
		IntegrationSettingsHandler.integrationSettings.clear();
		
		try(Stream<Path> walk = Files.walk(INTEGRATION_SETTINGS_FOLDER.toPath())) {
			walk.forEach((path) -> {
				File file = path.toFile();
				
				if (file.isFile()) {
					String fileName = file.getName();
					
					if (fileName.endsWith(".json")) {
						try {
							StringBuilder combined = new StringBuilder();
							
							Files.readAllLines(path).forEach((s) -> {
								combined.append(s);
							});
							
							IntegrationSettings settings = Util.GSON.fromJson(combined.toString(), IntegrationSettings.class);
							
							Integration integration = IntegrationManager.getIntegration(settings.id);
							updateSettings(settings, integration);
							
							IntegrationSettingsHandler.integrationSettings.add(settings);
						} catch (Exception e) {
							Logger.error("Error loading integration settings "+fileName+":", e);
						}
					}
				}
			});
		} catch(Exception e) {
			Logger.error("Error loading integration settings:", e);
		}
		//Add settings for any old integrations
		for (Integration integration : IntegrationManager.integrations) {
			if (integration.settings.size() > 0) {
				boolean found = false;
				
				for (IntegrationSettings settings : IntegrationSettingsHandler.integrationSettings) {
					if (integration.id.equals(settings.id)) {
						found = true;
						break;
					}
				}
				if (!found) {
					IntegrationSettings settings = new IntegrationSettings();
					settings.id = integration.id;
					updateSettings(settings, integration);
					IntegrationSettingsHandler.integrationSettings.add(settings);
				}
			}
		}
		//Update integration settings screen
		IntegrationSettingsScreen.INSTANCE.update();
	}
	
	private static void updateSettings(IntegrationSettings settings, Integration integration) {
		if (integration != null) {
			//Remove old settings
			List<ParameterConfigBase<?, ?>> toRemove = new ArrayList<ParameterConfigBase<?, ?>>();
			
			for (ParameterConfigBase<?, ?> cfg : settings.settings) {
				boolean found = false;
				
				for (ParameterBase<?, ?> setting : integration.settings) {
					if (cfg.parameterID.equals(setting.id)) {
						found = true;
						break;
					}
				}
				if (!found) {
					toRemove.add(cfg);
				}
			}
			for (ParameterConfigBase<?, ?> cfg : toRemove) {
				settings.settings.remove(cfg);
			}
			//Add new settings
			List<ParameterBase<?, ?>> toAdd = new ArrayList<ParameterBase<?, ?>>();
			
			for (ParameterBase<?, ?> setting : integration.settings) {
				boolean found = false;
				
				for (ParameterConfigBase<?, ?> cfg : settings.settings) {
					if (cfg.parameterID.equals(setting.id)) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(setting);
				}
			}
			for (ParameterBase<?, ?> setting : toAdd) {
				settings.settings.add(setting.createConfig());
			}
			
			//Add parameter info
			for (ParameterConfigBase settingConfig : settings.settings) {
				for (ParameterBase setting : integration.settings) {
					if (setting.id.equals(settingConfig.parameterID)) {
						settingConfig.parameter = setting;
					}
				}
			}
		}
	}

	public static void save(List<IntegrationSettings> settings) {
		saveInternal(settings);
		load();
	}
	
	private static void saveInternal(List<IntegrationSettings> settings) {
		//Clear folder
		if (INTEGRATION_SETTINGS_FOLDER.exists()) {
			File[] files = INTEGRATION_SETTINGS_FOLDER.listFiles();
			
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						file.delete();
					}
				}
			}
		}
		//Make sure folder exists
		INTEGRATION_SETTINGS_FOLDER.mkdirs();
		
		for (IntegrationSettings s : settings) {
			try {
				Files.write(new File(INTEGRATION_SETTINGS_FOLDER, s.id+".json").toPath(), Util.GSON.toJson(s).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
