package ingage.auth;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import ingage.connection.HTTPRequests;
import ingage.connection.StreamlabsSocket;
import ingage.connection.TwitchEventSub;
import ingage.connection.response.TwitchGetUsersResponse;

public class AuthManager {
	public static final String TWITCH_CLIENT_ID = "s0jyrybwcp9tm502ytm00sbg3n4jyh";
	private static final List<TwitchUser> twitchUsers = new ArrayList<TwitchUser>();
	private static final List<StreamlabsUser> streamlabsUsers = new ArrayList<StreamlabsUser>();
	
	private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
	private static final String TWITCH_AUTH_TOKENS_FILE = "./data/tokens/Twitch.json";
	private static final String STREAMLABS_AUTH_TOKENS_FILE = "./data/tokens/Streamlabs.json";
	
	public static List<TwitchUser> getTwitchUsers() {
		return twitchUsers;
	}
	
	public static List<StreamlabsUser> getStreamlabsUsers() {
		return streamlabsUsers;
	}
	
	public static void removeUser(TwitchUser user) {
		synchronized(twitchUsers) {
			AuthManager.twitchUsers.remove(user);
		}
		saveTwitch();
	}
	
	public static void removeUser(StreamlabsUser user) {
		synchronized(streamlabsUsers) {
			AuthManager.streamlabsUsers.remove(user);
			StreamlabsSocket.remove(user.token);
		}
		saveStreamlabs();
	}
	
	public static void twitchOAuth() {
		HTTPRequests.initTwitchOAuth(TWITCH_CLIENT_ID);
	}
	
	public static void onTwitchOAuth(String token, boolean save) {
		//Ignore if we already have the user
		for (TwitchUser user : twitchUsers) {
			if (user.token.equals(token)) {
				return;
			}
		}
		TwitchGetUsersResponse getUsers = HTTPRequests.getUsernameAndID(token);
		
		if (getUsers == null || getUsers.data.length == 0) {
			return;
		}
		
		TwitchUser user = new TwitchUser(token, getUsers.data[0].id, getUsers.data[0].display_name);
		
		twitchUsers.add(user);
		
		//Save the tokens now that there's a new one
		saveTwitch();
		
		//Add to existing connections
		TwitchEventSub.subscribeEventsForUser(user);
	}
	
	public static void addStreamLabs(String token) {
		//Ignore if we already have the user
		for (StreamlabsUser user : streamlabsUsers) {
			if (user.token.equals(token)) {
				return;
			}
		}
		//Ignore empty strings
		if (token.isEmpty()) {
			return;
		}
//		TwitchGetUsersResponse getUsers = HTTPRequests.getUsernameAndID(token);
//		
//		if (getUsers == null || getUsers.data.length == 0) {
//			return;
//		}
		
		StreamlabsUser user = new StreamlabsUser(token, "");
		
		streamlabsUsers.add(user);
		
		//Save the tokens now that there's a new one
		saveStreamlabs();
		
		//Add to existing connections
		StreamlabsSocket.add(token);
	}
	
	public static void saveTwitch() {
		synchronized(twitchUsers) {
			try {
				File file = new File(AuthManager.TWITCH_AUTH_TOKENS_FILE);
				file.getParentFile().mkdirs();
				
				JsonArray array = new JsonArray();
				
				for (TwitchUser user : AuthManager.twitchUsers) {
					array.add(user.token);
				}
				
				Files.asCharSink(file, StandardCharsets.UTF_8).write(GSON.toJson(array));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void saveStreamlabs() {
		synchronized(streamlabsUsers) {
			try {
				File file = new File(AuthManager.STREAMLABS_AUTH_TOKENS_FILE);
				file.getParentFile().mkdirs();
				
				JsonArray array = new JsonArray();
				
				for (StreamlabsUser user : AuthManager.streamlabsUsers) {
					array.add(user.token);
				}
				
				Files.asCharSink(file, StandardCharsets.UTF_8).write(GSON.toJson(array));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void load() {
		synchronized(twitchUsers) {
			try {
				File file = new File(AuthManager.TWITCH_AUTH_TOKENS_FILE);
				file.getParentFile().mkdirs();
				
				if (file.exists()) {
					String fromFile = Files.asCharSource(file, StandardCharsets.UTF_8).read();
					
					JsonArray tokens = GSON.fromJson(fromFile, JsonArray.class);
					
					//Validate each token
					for (int i = 0; i < tokens.size(); i++) {
						String token = tokens.get(i).getAsString();
						
						if (HTTPRequests.validateToken(token)) {
							AuthManager.onTwitchOAuth(token, false);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		synchronized(streamlabsUsers) {
			try {
				File file = new File(AuthManager.STREAMLABS_AUTH_TOKENS_FILE);
				file.getParentFile().mkdirs();
				
				if (file.exists()) {
					String fromFile = Files.asCharSource(file, StandardCharsets.UTF_8).read();
					
					JsonArray tokens = GSON.fromJson(fromFile, JsonArray.class);
					
					//Validate each token
					for (int i = 0; i < tokens.size(); i++) {
						String token = tokens.get(i).getAsString();
						
						//TODO: get username
						AuthManager.addStreamLabs(token);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		saveTwitch();
		saveStreamlabs();
	}
}
