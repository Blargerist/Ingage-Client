package ingage.integration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ingage.Logger;
import ingage.Util;
import ingage.integration.effect.EffectBase;
import ingage.integration.effect.parameter.ParameterBase;

public class IntegrationManager {

	public static final List<Integration> integrations = new ArrayList<Integration>();
	//For gui
	public static String[] integrationDisplayNames = new String[] {};
	public static final String INTEGRATIONS_FOLDER = "./integrations/";
	
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
	
	public static void load() {
		integrations.clear();
		
		File integrationsFolderFile = new File(INTEGRATIONS_FOLDER);
		integrationsFolderFile.mkdirs();
		
		try(Stream<Path> walk = Files.walk(integrationsFolderFile.toPath())) {
			walk.forEach((path) -> {
				File file = path.toFile();
				
				if (file.isFile()) {
					String fileName = file.getName();
					
					if (fileName.equalsIgnoreCase("Integration.json")) {
						try {
							StringBuilder combined = new StringBuilder();
							
							Files.readAllLines(path).forEach((s) -> {
								combined.append(s);
							});
							
							Integration integration = Util.GSON.fromJson(combined.toString(), Integration.class);
							
							if (integration != null) {
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
							e.printStackTrace();
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
}
