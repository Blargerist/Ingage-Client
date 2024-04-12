package ingage.connection;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import ingage.Logger;
import ingage.event.StreamlabsTipEvent;
import ingage.integration.EventHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public class StreamlabsSocket {
	
	private static Map<String, StreamlabsSocket> sockets = new HashMap<String, StreamlabsSocket>();
	
	private final String token;
	private Socket socket = null;
	private Dispatcher dispatcher = null;
	
	private StreamlabsSocket(String token) {
		this.token = token;
	}
	
	public static void add(String token) {
		StreamlabsSocket socket = new StreamlabsSocket(token);
		sockets.put(token, socket);
		socket.start();
	}
	
	public static void remove(String token) {
		StreamlabsSocket socket = sockets.remove(token);
		
		if (socket != null) {
			socket.startDestroyInternal();
			socket.waitDestroyInternal();
		}
	}
	
	private void start() {
		dispatcher = new Dispatcher();
		
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
			.dispatcher(dispatcher)
			.readTimeout(1, TimeUnit.MINUTES) // important for HTTP long-polling
			.build();
		
		URI uri = URI.create("https://sockets.streamlabs.com");
		
		IO.Options options = new IO.Options();
		options.callFactory = okHttpClient;
		options.webSocketFactory = okHttpClient;
		options.query = "token="+this.token;
		
		socket = IO.socket(uri, options);
		socket.on("event", (args) -> {
			JSONObject jObj = null;
			try {
				jObj = (JSONObject) args[0];
                String type = jObj.getString("type");

                //Ignore event types without a message array
                if (jObj.isNull("message") || type.equals("streamlabels") || type.equals("streamlabels.underlying") || type.equals("alertPlaying")) {
                    return;
                }

                String service;
                
                if (jObj.has("for")) {
                	service = jObj.getString("for");
                } else {
                	service = "";
                }
                
                //Ignore if there's no message
                if (!jObj.has("message")) {
                	return;
                }

                //Streamlabs may send "message" as either an array, or an object
                List<JSONObject> messageObjs = new ArrayList<JSONObject>();

                JSONArray messageArray = jObj.getJSONArray("message");
                
                if (messageArray != null) {
                	for (int i = 0; i < messageArray.length(); i++) {
                		messageObjs.add(messageArray.getJSONObject(i));
                	}
                } else {
                	JSONObject messageObj = jObj.getJSONObject("message");
                	
                	if (messageObj != null) {
                		messageObjs.add(messageObj);
                	}
                }

                for (JSONObject message : messageObjs) {
                	boolean test = false;
                	
                	if (message.has("isTest")) {
            			test = message.getBoolean("isTest");
                	}
                	
                    switch (service) {
                        case "streamlabs": {
                                switch (type) {
                                    case "donation": {
                                    		String name = message.getString("name");
                                            String from = message.getString("from");

                                            double amount = message.getDouble("amount");
                                            String msg = message.getString("message");

                                            StreamlabsTipEvent tip = new StreamlabsTipEvent(from, amount, msg, name);
                                            EventHandler.handleEvent(tip, test);
                                            break;
                                        }
                                    case "merch": {
                                            //TODO support
                                            break;
                                        }
                                    default:
                                        break;
                                }
                                break;
                            }
                        default: {
                                switch (type) {
                                    case "donation": {
                                			String name = message.getString("name");
                                            String from = message.getString("from");

                                            double amount = message.getDouble("amount");
                                            String msg = message.getString("message");
                                            
                                            StreamlabsTipEvent tip = new StreamlabsTipEvent(from, amount, msg, name);
                                            EventHandler.handleEvent(tip, test);

                                            break;
                                        }
                                    default:
                                        break;
                                }
                                break;
                            }
                    }
                }
            }
            catch (Exception e) {
                if (jObj != null) {
    				Logger.log("Streamlabs parsing error for: " + jObj.toString());
                }
                Logger.error(e);
            }
		});
		socket.on(Socket.EVENT_ERROR, (args) -> {
			Logger.log("Streamlabs socket error: "+args[0]);
		});
		socket.on(Socket.EVENT_CONNECT, (args) -> {
			Logger.log("Streamlabs socket connected");
		});
		socket.on(Socket.EVENT_CONNECT_ERROR, (args) -> {
			Logger.log("Streamlabs socket connection error: "+args[0]);
		});
		socket.on(Socket.EVENT_DISCONNECT, (args) -> {
			Logger.log("Streamlabs socket disconnected: "+args[0]);
		});
		socket.connect();
	}
	
	private void startDestroyInternal() {
		Socket socket = this.socket;
		
		if (socket != null) {
			socket.close();
		}
		//Shut down the executor service so the program can close slightly faster
		if (this.dispatcher != null) {
			this.dispatcher.executorService().shutdown();
		}
	}
	
	private void waitDestroyInternal() {
		
	}
	
	public static void startDestroy() {
		for (Entry<String, StreamlabsSocket> entry : StreamlabsSocket.sockets.entrySet()) {
			entry.getValue().startDestroyInternal();
		}
	}
	
	public static void waitDestroy() {
		for (Entry<String, StreamlabsSocket> entry : StreamlabsSocket.sockets.entrySet()) {
			entry.getValue().waitDestroyInternal();
		}
		StreamlabsSocket.sockets.clear();
	}
}
