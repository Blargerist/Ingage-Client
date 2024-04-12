package ingage.auth;

public class StreamlabsUser {

	public final String token;
	public final String username;
	
	public StreamlabsUser(String token, String username) {
		this.token = token;
		this.username = username;
	}
}
