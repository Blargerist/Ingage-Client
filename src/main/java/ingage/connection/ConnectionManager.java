package ingage.connection;

import ingage.ThreadManager;

public class ConnectionManager {

	public static void init() {
		TwitchOAuthHTTPServer.init();
		IntegrationWebSocketServer.init();
		IntegrationSocketServer.init();
	}
	
	public static void initTwitch() {
		TwitchEventSub.init();
		ThreadManager.initTwitchEventSubTimerThread();
	}
	
	public static void cleanUp() {
    	TwitchOAuthHTTPServer.startDestroy();
    	TwitchEventSub.startDestroy();
    	IntegrationWebSocketServer.startDestroy();
    	IntegrationSocketServer.startDestroy();
    	StreamlabsSocket.startDestroy();
    	
    	TwitchOAuthHTTPServer.waitDestroy();
    	TwitchEventSub.waitDestroy();
    	IntegrationWebSocketServer.waitDestroy();
    	IntegrationSocketServer.waitDestroy();
    	StreamlabsSocket.waitDestroy();
	}
}
