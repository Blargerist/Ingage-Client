package ingage.connection;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ingage.Logger;
import ingage.auth.AuthManager;
import ingage.connection.response.TwitchGetUsersResponse;

public class HTTPRequests {

	private static final Gson GSON = new Gson();

	public static void initTwitchOAuth(String clientID) {// TODO: Needs to use state
		String redirect_uri = "http://localhost:8080/User/ReceiveTwitchOAuth";
		String response_type = "token";
		String[] scopes = new String[] {
			"channel:read:redemptions",//For listening to channel points redemptions on eventsub
			"channel:read:hype_train",//For listening to hype train on eventsub
			"user:read:chat"//For listening to chat on eventsub
		};
		StringBuilder scopeBuilder = new StringBuilder();
		scopeBuilder.append(scopes[0]);

		for (int i = 1; i < scopes.length; i++) {
			scopeBuilder.append("+" + scopes[i]);
		}
		String force_verify = "true";
		// TODO Add state value to get

		String url = "https://id.twitch.tv/oauth2/authorize?response_type=" + response_type + "&client_id=" + clientID
				+ "&redirect_uri=" + redirect_uri + "&scope=" + scopeBuilder.toString() + "&force_verify="
				+ force_verify;

		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(URI.create(url));
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public static TwitchGetUsersResponse getUsernameAndID(String authToken) {
		TwitchGetUsersResponse responseData = null;

		try {
			URL url = new URL("https://api.twitch.tv/helix/users");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "Bearer " + authToken);
			connection.setRequestProperty("Client-Id", AuthManager.TWITCH_CLIENT_ID);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setInstanceFollowRedirects(false);

			try {
				// Executes the request
				int status = connection.getResponseCode();

				if (status == 200) {// Successful

					try (BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()))) {
						String line;
						StringBuffer content = new StringBuffer();
						while ((line = reader.readLine()) != null) {
							content.append(line);
						}
						responseData = GSON.fromJson(content.toString(), TwitchGetUsersResponse.class);
					}
				}
			} finally {
				connection.disconnect();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return responseData;
	}

	public static boolean validateToken(String authToken) {
		boolean success = false;

		try {
			URL url = new URL("https://id.twitch.tv/oauth2/validate");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "OAuth " + authToken);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setInstanceFollowRedirects(false);

			try {
				// Executes the request
				int status = connection.getResponseCode();

				if (status == 200) {// Successful
					success = true;
				}
			} finally {
				connection.disconnect();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return success;
	}

	public static boolean twitchEventSubSubscribeEvent(String authToken, JsonObject event) {
		boolean success = false;
		int status = 0;

		try {
			while (true) {
				status = twitchEventSubSubscribeEventInternal(authToken, event);
				
				//Success
				if (status == 202) {
					success = true;
					break;
				}
				//Bad request
				if (status == 400) {
					success = false;
					break;
				}
				//Not authorized
				if (status == 401) {
					success = false;
					break;
				}
				//Missing scopes
				if (status == 403) {
					success = false;
					break;
				}
				//Already subscribed
				if (status == 409) {
					success = false;
					break;
				}
				//Subscribed to too many things
				if (status == 429) {
					success = false;
					break;
				}
				Logger.log("Unhandled status "+status+" while sending eventsub subscription '"+event.get("type").getAsString()+"' for broadcaster "+event.get("condition").getAsJsonObject().get("broadcaster_user_id").getAsString());
				//Wait a bit before trying again
				Thread.sleep(10);
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		if (success) {
			Logger.log("Subscribed to eventsub subscription '"+event.get("type").getAsString()+"' for broadcaster "+event.get("condition").getAsJsonObject().get("broadcaster_user_id").getAsString());
		} else {
			Logger.log("Failed to subscribe to eventsub subscription '"+event.get("type").getAsString()+"' for broadcaster "+event.get("condition").getAsJsonObject().get("broadcaster_user_id").getAsString()+" with status "+status);
		}
		return success;
	}

	private static int twitchEventSubSubscribeEventInternal(String authToken, JsonObject event) {
		int status = 0;

		try {
			URL url = new URL("https://api.twitch.tv/helix/eventsub/subscriptions");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + authToken);
			con.setRequestProperty("Client-Id", AuthManager.TWITCH_CLIENT_ID);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setDoOutput(true);

			try (OutputStream os = con.getOutputStream()) {
				byte[] input = GSON.toJson(event).getBytes("utf-8");
				os.write(input, 0, input.length);
				// Executes the request
				status = con.getResponseCode();
			} finally {
				con.disconnect();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return status;
	}
}
