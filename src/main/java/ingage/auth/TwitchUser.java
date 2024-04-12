package ingage.auth;

public class TwitchUser {

	public final String token;
	public final String id;
	public final String username;
	
	public TwitchUser(String token, String id, String username) {
		this.token = token;
		this.id = id;
		this.username = username;
	}
}
