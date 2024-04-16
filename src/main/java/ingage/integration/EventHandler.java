package ingage.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import ingage.Logger;
import ingage.Util;
import ingage.connection.IntegrationSocketServer;
import ingage.connection.IntegrationWebSocketServer;
import ingage.data.DataManager;
import ingage.event.EventBase;
import ingage.event.HistoryManager;
import ingage.event.Metadata;
import ingage.gui.IntegrationEventsScreen;
import ingage.integration.effect.EffectBase;
import ingage.integration.effect.EffectBase.EffectConfig;
import ingage.integration.effect.EffectMessage;
import ingage.integration.effect.parameter.EffectListParameter;
import ingage.integration.effect.parameter.ParameterBase;
import ingage.integration.effect.parameter.ParameterBase.ParameterConfigBase;
import ingage.integration.effect.parameter.ParameterType;

public class EventHandler {
	
	public static final File PROFILES_FOLDER = new File("./profiles/");
//	public static List<Event> events = new ArrayList<Event>();
	public static List<Profile> profiles = new ArrayList<Profile>();

	public static void handleEvent(EventBase connectionEvent) {
		handleEvent(connectionEvent, false);
	}

	public static void handleEvent(EventBase connectionEvent, boolean replay) {
		try {
			if (!replay) {
				HistoryManager.handleEvent(connectionEvent);
				DataManager.handleEvent(connectionEvent);
			}
			//Create metadata
			Metadata metadata = connectionEvent.getMetadata();
			
			for (Profile profile : EventHandler.profiles) {
				//Skip disabled profiles
				if (!profile.enabled) {
					continue;
				}
				for (Event event : profile.events) {
					if (event.test(connectionEvent)) {
						//Send effects
						List<EffectMessage> list = event.getEffectMessages(connectionEvent);
						
						for (EffectMessage msg : list) {
							msg.toSend.add("metadata", Util.GSON.toJsonTree(metadata));
							
							JsonObject toSend = new JsonObject();
							toSend.addProperty("type", "EFFECT");
							toSend.add("payload", msg.toSend);
																			
							IntegrationWebSocketServer.queueIntegrationMessage(msg.integrationID, toSend);
							IntegrationSocketServer.queueIntegrationMessage(msg.integrationID, toSend);
						}
					}
				}
			}
		} catch(Exception e) {
			Logger.error(e);
		}
	}
	
	public static void load() {
		PROFILES_FOLDER.mkdirs();
		
		//Clear currently loaded profiles
		EventHandler.profiles.clear();
		
		try(Stream<Path> walk = Files.walk(PROFILES_FOLDER.toPath())) {
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
							
							Profile profile = Util.GSON.fromJson(combined.toString(), Profile.class);
														
							for (Event event : profile.events) {
								for (EffectConfig effect : event.effects) {
									setEffectConfigData(effect);
								}
							}
							EventHandler.profiles.add(profile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch(Exception e) {
			Logger.error("Error loading profiles:", e);
		}
		//Update profile configuration screen
		IntegrationEventsScreen.INSTANCE.updateProfiles();
	}
	
	private static void setEffectConfigData(EffectConfig effect) {
		Integration integration = IntegrationManager.getIntegration(effect.integrationID);
		
		if (integration != null) {
			effect.integration = integration;
			
			EffectBase effectBase = integration.getEffect(effect.effectID);
			
			if (effectBase != null) {
				effect.effect = effectBase;
				
				for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
					parameter.parameter = effectBase.getParameter(parameter.parameterID);
					
					//Update effects in lists
					if (parameter.type == ParameterType.EFFECT_LIST) {
						for (EffectConfig cfg : ((EffectListParameter.Config)parameter).value) {
							setEffectConfigData(cfg);
						}
					}
				}
				
				//Add any missing parameters to the effect config
				outer:
				for (ParameterBase<?, ?> p : effectBase.parameters) {
					for (ParameterConfigBase<?, ?> parameter : effect.parameters) {
						if (parameter.parameterID.equals(p.id)) {
							continue outer;
						}
					}
					effect.parameters.add(p.createConfig());
				}
			}
		}
	}

	public static void save(List<Profile> profiles) {
		saveInternal(profiles);
		load();
	}
	
	private static void saveInternal(List<Profile> profiles) {
		//Clear folder
		if (PROFILES_FOLDER.exists()) {
			File[] files = PROFILES_FOLDER.listFiles();
			
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						file.delete();
					}
				}
			}
		}
		//Make sure folder exists
		PROFILES_FOLDER.mkdirs();
		
		for (Profile profile : profiles) {
			try {
				Files.write(new File(PROFILES_FOLDER, profile.name+".json").toPath(), Util.GSON.toJson(profile).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
