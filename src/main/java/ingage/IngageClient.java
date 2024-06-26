package ingage;

import ingage.auth.AuthManager;
import ingage.connection.ConnectionManager;
import ingage.data.DataManager;
import ingage.event.HistoryManager;
import ingage.integration.EventHandler;
import ingage.integration.IntegrationManager;
import ingage.integration.IntegrationSettingsHandler;

public class IngageClient {
	
	public static boolean shutdown = false;
	
    public static void main(String[] args) {
    	try {
        	try {
            	ConnectionManager.init();
        	} catch(Exception e) {
        		shutdown = true;
        		//If we can't make the initial connections, just shut down
        		ConnectionManager.cleanUp();
        		return;
        	}
        	//Load config
        	ConfigManager.load();
        	//Initialize history manager
        	HistoryManager.init();
        	//Initialize data manager
        	DataManager.init();
        	//Load integrations
        	IntegrationManager.load();
        	try {
            	//Load events
            	EventHandler.load();
            	//Load integration settings
            	IntegrationSettingsHandler.load();
        	} catch (Exception e) {
        		shutdown = true;
        		ConnectionManager.cleanUp();
        		return;
        	}
        	
        	//Load saved auth tokens
        	AuthManager.load();
        	//Start twitch connections
        	ConnectionManager.initTwitch();
        	//Start sending integration messages
        	ThreadManager.initIntegrationServerQueueThread();
        	
        	Window.launch();
        	
        	//Clean up
    		shutdown = true;
        	ThreadManager.shutdownTwitchEventSubTimerThread();
        	ThreadManager.shutdownExecutor();
        	ConnectionManager.cleanUp();
        	DataManager.save();
        	ConfigManager.save();
    	} catch (Exception e) {
    		Logger.error(e);
    	}
    }
}
