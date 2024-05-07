package ingage.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.type.ImString;
import ingage.Logger;
import ingage.gui.IntegrationEventsScreen;

public class Profile {
	public final transient String uuid = UUID.randomUUID().toString();

	public String name = "Profile";
	public boolean enabled = true;
	public final List<Event> events = new ArrayList<Event>();
    private transient List<Event> eventsToRemove = new ArrayList<Event>();
    private transient List<Event> eventsToAdd = new ArrayList<Event>();
    private transient List<Event> eventsToMoveUp = new ArrayList<Event>();
    private transient List<Event> eventsToMoveDown = new ArrayList<Event>();

	public void imGui() {		
		try {
			ImString name = new ImString(this.name, 1000);
			
			if (ImGui.inputText("Name", name)) {
				this.name = getFreeProfileName(name.get().replace("[^a-zA-Z0-9\\.\\-]", ""), this);
			}
			
			if (ImGui.radioButton("Enabled", this.enabled)) {
				this.enabled =! this.enabled;
			}
			
			if (ImGui.treeNode("Events")) {
				if (ImGui.button("New Event")) {
					eventsToAdd.add(new Event());
				}
				
				for (Event event : events) {
					if (ImGui.treeNode(event.uuid, event.name)) {
						
						if (ImGui.button("Remove")) {
							eventsToRemove.add(event);
						}
						event.imGui();
						
						ImGui.treePop();
					}
				}
				
				//Move components up
				for (Event event : eventsToMoveUp) {
					int index = events.indexOf(event);
					Collections.swap(events, events.indexOf(event), Math.max(0, index - 1));
				}
				eventsToMoveUp.clear();
				
				//Move components down
				for (Event event : eventsToMoveDown) {
					int index = events.indexOf(event);
					Collections.swap(events, events.indexOf(event), Math.min(events.size() - 1, index + 1));
				}
				eventsToMoveDown.clear();
				
				//Remove deleted components
				for (Event event : eventsToRemove) {
					events.remove(event);
				}
				eventsToRemove.clear();
				
				//Add new components
				events.addAll(eventsToAdd);
				eventsToAdd.clear();
				
				ImGui.treePop();
			}
		} catch(Exception e) {
			Logger.error("Error configuring events", e);
		}
	}
	
	public Profile clone() {
		Profile profile = new Profile();
		profile.name = this.name;
		profile.enabled = this.enabled;

		profile.events.addAll(this.events.stream().map((e) -> { return e.clone(); }).collect(Collectors.toList()));
		
		return profile;
	}
	
	public static String getFreeProfileName(String name) {
		return getFreeProfileName(name, null);
	}
	
	private static String getFreeProfileName(String name, Profile ignoreProfile) {
		int index = 1;
		String newName = name;
		
		//If profile in use, add (index) to the end of it and check again
		while (profileNameInUse(newName, ignoreProfile)) {
			index++;
			newName = name+" ("+index+")";
		}
		return newName;
	}
	
	private static boolean profileNameInUse(String name, Profile ignoreProfile) {
		List<Profile> profiles = IntegrationEventsScreen.INSTANCE.profiles;
		
		for (Profile profile : profiles) {
			//Ignore ourselves
			if (profile == ignoreProfile) {
				continue;
			}
			if (profile.name.equals(name)) {
				return true;
			}
		}
		return false;
	}
}
