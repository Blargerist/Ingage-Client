package ingage.gui;

import java.util.ArrayList;
import java.util.List;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import ingage.auth.AuthManager;
import ingage.auth.StreamlabsUser;
import ingage.auth.TwitchUser;

public class UsersScreen extends Screen {
	
	public static final UsersScreen INSTANCE = new UsersScreen();
	
	private List<TwitchUser> twitchUsers = new ArrayList<TwitchUser>();
	private List<StreamlabsUser> streamlabsUsers = new ArrayList<StreamlabsUser>();

	@Override
	public void imGui() {
		//Copy user lists in case they're modified while we're rendering
		twitchUsers.clear();
		twitchUsers.addAll(AuthManager.getTwitchUsers());
		streamlabsUsers.clear();
		streamlabsUsers.addAll(AuthManager.getStreamlabsUsers());
		
		super.imGui();
				
		if (ImGui.button("Add Twitch Account")) {
			AuthManager.twitchOAuth();
		}
		//Add streamlabs account
		if (ImGui.beginPopup("Add Streamlabs Account")) {
			ImString token = new ImString(1000);
			
			if (ImGui.inputText("Token", token, ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.Password)) {
				AuthManager.addStreamLabs(token.get());
				
				ImGui.closeCurrentPopup();
			}
			ImGui.endPopup();
		}
    	ImGui.sameLine();
		
        if (ImGui.button("Add Streamlabs Account")) {
			ImGui.openPopup("Add Streamlabs Account");
        }
		
		for (TwitchUser user : twitchUsers) {
			if (ImGui.treeNode("Twitch: "+user.username+"##"+user.id)) {
				
				if (ImGui.button("Remove")) {
					AuthManager.removeUser(user);
				}
				
				ImGui.text("ID: ");
				ImGui.sameLine();
				ImGui.text(user.id);
				
				ImGui.text("Username: ");
				ImGui.sameLine();
				ImGui.text(user.username);
				
				ImGui.treePop();
			}
		}
		int index = 0;
		for (StreamlabsUser user : streamlabsUsers) {
			if (ImGui.treeNode("StreamLabs: "+user.username+"##"+index)) {
				
				if (ImGui.button("Remove")) {
					AuthManager.removeUser(user);
				}
				
				ImGui.text("Username: ");
				ImGui.sameLine();
				ImGui.text(user.username);
				
				ImGui.treePop();
			}
			index++;
		}
	}
}
