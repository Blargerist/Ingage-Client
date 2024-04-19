package ingage.event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.google.gson.reflect.TypeToken;

import ingage.Logger;
import ingage.Util;

public class HistoryManager {
	public static final File HISTORY_FILE = new File("./data/ConnectionEvents.json");
	private static HashMap<String, CircularFifoQueue<EventBase>> connectionEventHistory = new HashMap<String, CircularFifoQueue<EventBase>>();
	
	public static void handleEvent(EventBase event) {
		synchronized(connectionEventHistory) {
			CircularFifoQueue<EventBase> queue = connectionEventHistory.get(event.getType().name());
			
			if (queue == null) {
				queue = new CircularFifoQueue<EventBase>(100);
				connectionEventHistory.put(event.getType().name(), queue);
			}
			queue.add(event);
		}
		save();
	}
	
	public static void cloneConnectionEventHistory(List<EventBase> list) {
		synchronized(connectionEventHistory) {
			for (Entry<String, CircularFifoQueue<EventBase>> e : connectionEventHistory.entrySet()) {
				for (EventBase event : e.getValue()) {
					list.add(event);
				}
			}
		}
	}
	
	public static void init() {
		load();
	}
	
	private static void load() {
		synchronized(connectionEventHistory) {
			HISTORY_FILE.getParentFile().mkdirs();
			
			if (HISTORY_FILE.exists()) {
				try {
					StringBuilder combined = new StringBuilder();
					
					Files.readAllLines(HISTORY_FILE.toPath()).forEach((s) -> {
						combined.append(s);
					});
					
					//Load map using Lists instead of queues
					HashMap<String, List<EventBase>> map = Util.GSON.fromJson(combined.toString(), new TypeToken<HashMap<String, List<EventBase>>>() {}.getType());
					
					connectionEventHistory.clear();
					
					//Create queues with the correct size and copy lists to them
					for (Entry<String, List<EventBase>> e : map.entrySet()) {
						CircularFifoQueue<EventBase> queue = new CircularFifoQueue<EventBase>(100);
						queue.addAll(e.getValue());
						connectionEventHistory.put(e.getKey(), queue);
					}
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		}
	}
	
	public static void save() {
		synchronized(connectionEventHistory) {
			HISTORY_FILE.getParentFile().mkdirs();
			
			try {
				Files.write(HISTORY_FILE.toPath(), Util.GSON.toJson(connectionEventHistory).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
