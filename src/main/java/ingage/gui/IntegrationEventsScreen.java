package ingage.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import imgui.ImGui;
import ingage.Logger;
import ingage.integration.EventHandler;
import ingage.integration.Profile;

public class IntegrationEventsScreen extends Screen {
	
	public static final IntegrationEventsScreen INSTANCE = new IntegrationEventsScreen();
	
	public final List<Profile> profiles = new ArrayList<Profile>();
    private transient List<Profile> profilesToRemove = new ArrayList<Profile>();
    private transient List<Profile> profilesToAdd = new ArrayList<Profile>();
    private transient List<Profile> profilesToMoveUp = new ArrayList<Profile>();
    private transient List<Profile> profilesToMoveDown = new ArrayList<Profile>();
	
	public void updateProfiles() {
		this.profiles.clear();
		this.profiles.addAll(EventHandler.profiles.stream().map((p) -> { return p.clone(); }).collect(Collectors.toList()));
	}

	@Override
	public void imGui() {
		super.imGui();
		boolean cancel = false;
		
		try {
			if (ImGui.button("Save")) {
				EventHandler.save(this.profiles);
			}
			ImGui.sameLine();
			
			if (ImGui.button("Cancel")) {
				cancel = true;
			}
			if (ImGui.button("New Profile")) {
				Profile profile = new Profile();
				profile.name = Profile.getFreeProfileName(profile.name);
				profilesToAdd.add(profile);
			}
			
			for (Profile profile : profiles) {
				if (ImGui.treeNode(profile.uuid, profile.name)) {
					
					if (ImGui.button("Remove")) {
						profilesToRemove.add(profile);
					}
					profile.imGui();
					
					ImGui.treePop();
				}
			}
			
			//Move profiles up
			for (Profile profile : profilesToMoveUp) {
				int index = profiles.indexOf(profile);
				Collections.swap(profiles, profiles.indexOf(profile), Math.max(0, index - 1));
			}
			profilesToMoveUp.clear();
			
			//Move profiles down
			for (Profile profile : profilesToMoveDown) {
				int index = profiles.indexOf(profile);
				Collections.swap(profiles, profiles.indexOf(profile), Math.min(profiles.size() - 1, index + 1));
			}
			profilesToMoveDown.clear();
			
			//Remove deleted profiles
			for (Profile profile : profilesToRemove) {
				profiles.remove(profile);
			}
			profilesToRemove.clear();
			
			//Add new profiles
			profiles.addAll(profilesToAdd);
			profilesToAdd.clear();
		} catch(Exception e) {
			Logger.error("Error configuring profiles", e);
		}
		if (cancel) {
			this.updateProfiles();
		}
	}
}
