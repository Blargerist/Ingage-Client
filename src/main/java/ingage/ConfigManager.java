package ingage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class ConfigManager {

	public static final File CONFIG_FILE = new File(Const.WORKING_DIRECTORY+"Config.json");
	public static ConfigManager INSTANCE = new ConfigManager();
	private final HashMap<String, Window> windows = new HashMap<String, Window>();
	
	public Window getWindow(String id) {
		Window window = windows.get(id);
		
		if (window == null) {
			window = new Window();
			windows.put(id, window);
		}
		return window;
	}
	
	public static void load() {
		CONFIG_FILE.getParentFile().mkdirs();
		
		if (CONFIG_FILE.exists()) {
			try {
				StringBuilder combined = new StringBuilder();
				
				Files.readAllLines(CONFIG_FILE.toPath()).forEach((s) -> {
					combined.append(s);
				});
				
				ConfigManager.INSTANCE = Util.GSON.fromJson(combined.toString(), ConfigManager.class);
			} catch (Exception e) {
				Logger.error(e);
			}
		}
	}
	
	public static void save() {
		CONFIG_FILE.getParentFile().mkdirs();
		
		try {
			Files.write(CONFIG_FILE.toPath(), Util.GSON.toJson(ConfigManager.INSTANCE).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static class Window {
		public int width = 945;
		public int height = 630;
		public int x = Integer.MIN_VALUE;
		public int y = Integer.MIN_VALUE;
	}
}
