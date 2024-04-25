package ingage;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import imgui.ImGui;
import imgui.ImGuiIO;
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
        
        ConfigManager.Window window = ConfigManager.INSTANCE.getWindow("Main");
        
        if (window != null) {
        	config.setWidth(window.width);
        	config.setHeight(window.height);
        }
    }
    
    @Override
    protected void preRun() {
    	//Get rid of the ini file
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        
        //Set window icon
        try {
        	Util.setWindowIcon(this.handle, "assets/icons/icon16.png", "assets/icons/icon32.png");
        } catch (Exception e) {
        	Logger.error(e);
        }
        
        //Update window position
    	ConfigManager.Window window = ConfigManager.INSTANCE.getWindow("Main");
    	
    	if (window.x > Integer.MIN_VALUE && window.y > Integer.MIN_VALUE) {
            GLFW.glfwSetWindowPos(handle, window.x, window.y);
    	}
    }
    
    @Override
    protected void preProcess() {
        ImGuiViewport viewport = ImGui.getMainViewport();

        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowViewport(viewport.getID());

    	//Update window size and pos in the config
    	ConfigManager.Window window = ConfigManager.INSTANCE.getWindow("Main");
    	window.width = (int) viewport.getWorkSizeX();
    	window.height = (int) viewport.getWorkSizeY();
    	
    	IntBuffer x = BufferUtils.createIntBuffer(1);
    	IntBuffer y = BufferUtils.createIntBuffer(1);
    	GLFW.glfwGetWindowPos(this.handle, x, y);
    	
    	window.x = x.get();
    	window.y = y.get();
        
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
}
