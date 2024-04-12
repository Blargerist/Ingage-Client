package ingage;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import ingage.gui.Screen;
import ingage.gui.UsersScreen;

public class Window extends Application {
	
	public static final Window INSTANCE = new Window();
	public static Screen screen = UsersScreen.INSTANCE;
	
    @Override
    protected void configure(Configuration config) {
        config.setTitle("Ingage Client");
    }
    
    @Override
    protected void preRun() {
    	
    }
    
    @Override
    protected void preProcess() {
        ImGuiViewport viewport = ImGui.getMainViewport();

        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowViewport(viewport.getID());
        
    	ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0F);
    	ImGui.begin("Main Window", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoResize);
    }

    @Override
    public void process() {
    	try {
        	if (screen != null) {
        		screen.imGui();
        	}
    	} catch(Exception e) {
    		Logger.error("Exception while rendering screen", e);
    	}
    }
    
    @Override
    protected void postProcess() {
    	ImGui.end();
    	ImGui.popStyleVar();
    }
    
    public static void launch() {
    	Window.launch(Window.INSTANCE);
    }
//    
//    public static void launch(final Window app) {        
//        app.preRun();
//        app.run();
//        app.postRun();
//        app.dispose();
//    }
//
//    public static void initializeWindow(final Window app) {
//        final Configuration config = new Configuration();
//        app.configure(config);
//        app.init(config);
//    }
}
