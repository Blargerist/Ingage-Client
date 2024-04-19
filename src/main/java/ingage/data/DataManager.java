package ingage.data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import com.google.common.io.Files;

import ingage.Logger;
import ingage.Util;
import ingage.event.ChannelPointRedemptionEvent;
import ingage.event.ChatEvent;
import ingage.event.ChatNotificationEvent;
import ingage.event.EventBase;
import ingage.event.HypeTrainEndEvent;
import ingage.event.StreamlabsTipEvent;

public class DataManager {

	private static final Object lockObj = new Object();
	public static final Calendar lastSave = Calendar.getInstance();
	private static Data currentDayData = new Data();
	private static int currentYear = lastSave.get(Calendar.YEAR);
	private static int currentMonth = lastSave.get(Calendar.MONTH);
	private static int currentDayOfMonth = lastSave.get(Calendar.DAY_OF_MONTH);
	private static File currentDayFile = new File("./data/stats/"+currentYear+"/"+String.format("%02d", currentMonth+1)+"/"+String.format("%02d", currentDayOfMonth)+".json");
	
	public static void init() {
		load();
	}
	
	public static void save() {
		saveDay();
	}
	
	public static void handleEvent(EventBase connectionEvent) {
		synchronized(lockObj) {
			Calendar now = Calendar.getInstance();
			
			//If it's a new day
			if (now.get(Calendar.YEAR) != currentYear || now.get(Calendar.MONTH) != currentMonth || now.get(Calendar.DAY_OF_MONTH) != currentDayOfMonth) {
				//Save the current day
				save();
				
				//Create new data object
				currentDayData = new Data();
				//Update year/month/day to the new values
				currentYear = lastSave.get(Calendar.YEAR);
				currentMonth = lastSave.get(Calendar.MONTH);
				currentDayOfMonth = lastSave.get(Calendar.DAY_OF_MONTH);
				//Update the file name
				currentDayFile = new File("./data/"+currentYear+"/"+String.format("%02d", currentMonth)+"/"+String.format("%02d", currentDayOfMonth)+".json");
			}
			//Chat messages
			if (connectionEvent instanceof ChatEvent) {
				ChatEvent event = (ChatEvent)connectionEvent;
				currentDayData.chatMessages++;
				
				if (event.isBits()) {
					currentDayData.bits += event.getBits();
				}
			}
			//Chat notifications
			if (connectionEvent instanceof ChatNotificationEvent) {
				ChatNotificationEvent event = (ChatNotificationEvent)connectionEvent;
				
				if (event.isSub()) {
					int tier = event.getSubTier().ordinal()+1;
					
					switch(tier) {
						case 1:
							currentDayData.tier1Subs++;
							break;
						case 2:
							currentDayData.tier2Subs++;
							break;
						case 3:
							currentDayData.tier3Subs++;
							break;
					}
				}
			}
			//Channel point redemptions
			if (connectionEvent instanceof ChannelPointRedemptionEvent) {
				ChannelPointRedemptionEvent event = (ChannelPointRedemptionEvent)connectionEvent;
				
				currentDayData.channelPointRedemptions++;
				currentDayData.channelPointsSpent += event.getCost();
			}
			//Hype train
			if (connectionEvent instanceof HypeTrainEndEvent) {
				HypeTrainEndEvent event = (HypeTrainEndEvent)connectionEvent;
				
				currentDayData.hypeTrains++;
				currentDayData.hypeTrainTotalLevels += event.level;
				
				if (event.level > currentDayData.hypeTrainHighestLevel) {
					currentDayData.hypeTrainHighestLevel = event.level;
				}
			}
			//Streamlabs tips
			if (connectionEvent instanceof StreamlabsTipEvent) {
				StreamlabsTipEvent event = (StreamlabsTipEvent)connectionEvent;
				
				currentDayData.streamLabsTips += event.amount;
			}
			//Save new data
			save();
		}
	}
	
	private static void load() {
		try {
			currentDayFile.getParentFile().mkdirs();
			
			if (currentDayFile.exists()) {
				String fromFile = Files.asCharSource(currentDayFile, StandardCharsets.UTF_8).read();
				
				Data data = Util.GSON.fromJson(fromFile, Data.class);
				
				if (data != null) {
					DataManager.currentDayData = data;
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
	
	private static void saveDay() {
		try {
			currentDayFile.getParentFile().mkdirs();
			
			Files.asCharSink(currentDayFile, StandardCharsets.UTF_8).write(Util.GSON.toJson(currentDayData));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
