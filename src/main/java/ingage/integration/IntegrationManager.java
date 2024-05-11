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
import ingage.integration.effect.EffectBase;
import ingage.integration.effect.parameter.BooleanParameter;
import ingage.integration.effect.parameter.EffectListParameter;
import ingage.integration.effect.parameter.ParameterBase;
import ingage.integration.effect.parameter.ParameterType;

public class IntegrationManager {

	public static final List<Integration> integrations = new ArrayList<Integration>();
	//For gui
	public static String[] integrationDisplayNames = new String[] {};
	public static final String INTEGRATIONS_FOLDER = "./data/integrations/";
	
	public static Integration getIntegration(String id) {
		for (Integration integration : integrations) {
			if (integration.id.equals(id)) {
				return integration;
			}
		}
		return null;
	}
	
	public static void imGui() {
		
	}
	
	public static void addIntegration(Integration integration) {
		File integrationsFolder = new File(IntegrationManager.INTEGRATIONS_FOLDER);
		integrationsFolder.mkdirs();
		
		//Save new integration
		try {
			Files.write(new File(integrationsFolder, integration.id+".json").toPath(), Util.GSON.toJson(integration).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (IOException e) {
			Logger.error(e);
		}
		
		//Add settings for new integration
		IntegrationSettingsHandler.addSettings(integration);
		
		//Reload all integrations
		load();
	}
	
	public static void load() {
		integrations.clear();
		
		loadInternalIntegration();
		
		File integrationsFolderFile = new File(INTEGRATIONS_FOLDER);
		integrationsFolderFile.mkdirs();
		
		try(Stream<Path> walk = Files.walk(integrationsFolderFile.toPath())) {
			walk.forEach((path) -> {
				File file = path.toFile();
				
				if (file.isFile()) {
					String fileName = file.getName();
					
					if (fileName.endsWith(".json") && !file.getParent().endsWith("settings")) {
						try {
							StringBuilder combined = new StringBuilder();
							
							Files.readAllLines(path).forEach((s) -> {
								combined.append(s);
							});
							
							Integration integration = Util.GSON.fromJson(combined.toString(), Integration.class);
							
							if (integration != null && integration.id != null && integration.display != null) {
								for (EffectBase effect : integration.effects) {
									effect.integration = integration;
									
									for (ParameterBase<?, ?> parameter : effect.parameters) {
										parameter.effect = effect;
									}
								}
								integration.buildDisplayNamesArray();
								
								IntegrationManager.integrations.add(integration);
							}
						} catch (Exception e) {
							Logger.error(e);
						}
					}
				}
			});
		} catch(Exception e) {
			Logger.error("Error loading integrations:", e);
		}
		IntegrationManager.integrationDisplayNames = IntegrationManager.integrations.stream().map((i) -> {
			return i.display;
		}).toArray(String[]::new);
	}
	
	public static void loadInternalIntegration() {
		Integration integration = new Integration("ingage", "Ingage");
		
		EffectBase listEffect = new EffectBase();
		listEffect.id = "list";
		listEffect.display = "List";
		listEffect.integration = integration;
		
		EffectListParameter listParameter = new EffectListParameter();
		listParameter.integration = integration;
		listParameter.effect = listEffect;
		listParameter.type = ParameterType.EFFECT_LIST;
		listParameter.id = "effects";
		listParameter.display = "Effects";
		listParameter.description = "A list of effects to run";
		listParameter.required = true;
		listParameter.defaultValue = null;
		
		listEffect.parameters.add(listParameter);
		
		BooleanParameter weightedParameter = new BooleanParameter();
		weightedParameter.integration = integration;
		weightedParameter.effect = listEffect;
		weightedParameter.type = ParameterType.BOOLEAN;
		weightedParameter.id = "weighted";
		weightedParameter.display = "Weighted";
		weightedParameter.description = "If weighted, chooses one random effect from the list based on their relative weights each time a condition is met. If not, chooses all effects.";
		weightedParameter.required = true;
		weightedParameter.defaultValue = null;
		
		listEffect.parameters.add(weightedParameter);
		
		integration.effects.add(listEffect);
		
		integration.buildDisplayNamesArray();
		
		integrations.add(integration);
	}
}
