package ingage.connection.response;

public class TwitchGetUsersResponse {
	
	public Data[] data;
	
	public static class Data {
		public String id;
		public String logic;
		public String display_name;
		public String type;
		public String broadcaster_type;
		public String description;
		public String profile_image_url;
		public String offline_image_url;
//		public int view_count;//Deprecated
		public String email;
		public String created_at;
	}
}
