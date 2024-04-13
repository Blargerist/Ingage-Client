package ingage.connection;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ingage.Logger;
import ingage.auth.AuthManager;
import ingage.auth.TwitchUser;
import ingage.event.ChannelPointRedemptionEvent;
import ingage.event.ChatEvent;
import ingage.event.ChatNotificationEvent;
import ingage.event.EventBase;
import ingage.integration.EventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;

public class TwitchEventSub extends SimpleChannelInboundHandler<Object> {
	
	public static Instant lastMessageTime = Instant.now();
	
	private static EventLoopGroup group = null;
	private static final String NONCE = UUID.randomUUID().toString();
	private static String sessionID;
	
	private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public TwitchEventSub(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                Logger.log("WebSocket Client connected!");
                handshakeFuture.setSuccess();
                
//                //Listen
//                JSONObject listen = new JSONObject();
//        		JSONObject data = new JSONObject();
//        		JSONArray topics = new JSONArray();
//        		try {
//        			listen.put("type", "LISTEN");
//        			listen.put("nonce", NONCE);
//        			listen.put("data", data);
//        			twitchChannelID = AuthManager.twitchChannelID;
//        			topics.put("channel-points-channel-v1." + AuthManager.twitchChannelID);
//        			topics.put("channel-bits-events-v2." + AuthManager.twitchChannelID);
//        			topics.put("channel-subscribe-events-v1." + AuthManager.twitchChannelID);
//        			data.put("topics", topics);
//        			data.put("auth_token", AuthManager.Keys.getTwitchAuthToken());
//        			WebSocketFrame frame = new TextWebSocketFrame(listen.toString());
//                    ch.writeAndFlush(frame);
//                    
//                    ping(ctx);
//        		} catch (JSONException e) {
//        			Logger.error(e);
//        		}
            } catch (WebSocketHandshakeException e) {
                Logger.log("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
        	//Reset the timer
        	lastMessageTime = Instant.now();
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            boolean messageHandled = false;
            
            try {
				JsonObject json = JsonParser.parseString(textFrame.text()).getAsJsonObject();
				
				if (json.has("metadata") && json.has("payload")) {
					//TODO: Check time for message to make sure it isn't old
					JsonObject meta = json.get("metadata").getAsJsonObject();
					JsonObject payload = json.get("payload").getAsJsonObject();
					
					if (meta.has("message_type")) {
						String msgType = meta.get("message_type").getAsString();
						
						switch (msgType) {
							case "session_welcome": {
								if (payload.has("session")) {
									JsonObject session = payload.get("session").getAsJsonObject();

									String status = session.get("status").getAsString();
									String id = session.get("id").getAsString();
									int keepaliveTimeout = session.get("keepalive_timeout_seconds").getAsInt();
									
									if (status.equals("connected") && keepaliveTimeout > 0) {
										messageHandled = true;
										sessionID = id;
										
										List<TwitchUser> users = AuthManager.getTwitchUsers().stream().collect(Collectors.toList());//Create clone of list in case it's modified
										
										for (TwitchUser user : users) {
											subscribeEventsForUser(user);
										}
									}
								}
								break;
							}
							case "notification": {
								if (meta.has("subscription_type")) {
									String subType = meta.get("subscription_type").getAsString();
									EventBase event = null;
									
									switch (subType) {
										case "channel.chat.message": {
											event = ChatEvent.fromJson(payload.get("event").getAsJsonObject());
											break;
										}
										case "channel.chat.notification": {
											event = ChatNotificationEvent.fromJson(payload.get("event").getAsJsonObject());
											break;
										}
										case "channel.channel_points_custom_reward_redemption.add": {
											event = ChannelPointRedemptionEvent.fromJson(payload.get("event").getAsJsonObject());
											break;
										}
									}
									
									if (event != null) {
										messageHandled = true;
										EventHandler.handleEvent(event);
									}
								}
							}
							case "session_keepalive": {
								messageHandled = true;
							}
						}
					}
				}
            } catch (Exception e) {
            	Logger.error(e);
            }
            if (!messageHandled) {
                Logger.log("Unhandled Twitch EventSub message: " + textFrame.text());
            }
            
        } else if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content()));
        } else if (frame instanceof PongWebSocketFrame) {
            Logger.log(" Twitch EventSub received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            Logger.log(" Twitch EventSub received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error(cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
    
    public static void subscribeEventsForUser(TwitchUser user) {
    	if (TwitchEventSub.sessionID != null) {
    		//Chat message
    		{
    			JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.chat.message");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		condition.addProperty("user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
    			HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    		
    		//Chat notification
    		{
        		JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.chat.notification");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		condition.addProperty("user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
        		HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    		
    		//Channel point redemption
    		{
        		JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.channel_points_custom_reward_redemption.add");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
        		HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    		
    		//Hype Train Begin
    		{
        		JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.hype_train.begin");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
        		HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    		
    		//Hype Train Progress
    		{
        		JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.hype_train.progress");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
        		HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    		
    		//Hype Train End
    		{
        		JsonObject json = new JsonObject();
        		json.addProperty("type", "channel.hype_train.end");
        		json.addProperty("version", "1");
        		
        		JsonObject condition = new JsonObject();
        		json.add("condition", condition);
        		condition.addProperty("broadcaster_user_id", user.id);
        		
        		JsonObject transport = new JsonObject();
        		json.add("transport", transport);
        		transport.addProperty("method", "websocket");
        		transport.addProperty("session_id", TwitchEventSub.sessionID);
        		
        		HTTPRequests.twitchEventSubSubscribeEvent(user.token, json);
    		}
    	}
    }
	
	public static void init() {
		lastMessageTime = Instant.now();
		startDestroy();
		waitDestroy();
		
		synchronized(lastMessageTime) {
			int port = 443;
	        try {
	    		URI uri = new URI("wss://eventsub.wss.twitch.tv/ws");
	            final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();

	            final SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

	            group = new NioEventLoopGroup();
	            
	            final TwitchEventSub handler =
	                    new TwitchEventSub(
	                            WebSocketClientHandshakerFactory.newHandshaker(
	                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

	            Bootstrap b = new Bootstrap();
	            b.group(group)
	             .channel(NioSocketChannel.class)
	             .handler(new ChannelInitializer<SocketChannel>() {
	                 @Override
	                 protected void initChannel(SocketChannel ch) {
	                     ChannelPipeline p = ch.pipeline();
	                     if (sslCtx != null) {
	                         p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
	                     }
	                     p.addLast(
	                             new HttpClientCodec(),
	                             new HttpObjectAggregator(8192),
	                             WebSocketClientCompressionHandler.INSTANCE,
	                             handler);
	                 }
	             });

	            ChannelFuture f = b.connect(uri.getHost(), port).sync();
	            handler.handshakeFuture().sync();

	            Logger.log("PubSub Socket bound");

//	            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
//	            while (true) {
//	                String msg = console.readLine();
//	                if (msg == null) {
//	                    break;
//	                } else if ("bye".equals(msg.toLowerCase())) {
//	                    ch.writeAndFlush(new CloseWebSocketFrame());
//	                    ch.closeFuture().sync();
//	                    break;
//	                } else if ("ping".equals(msg.toLowerCase())) {
//	                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
//	                    ch.writeAndFlush(frame);
//	                } else {
//	                    WebSocketFrame frame = new TextWebSocketFrame(msg);
//	                    ch.writeAndFlush(frame);
//	                }
//	            }
	        } catch (Exception e) {
	        	Logger.error(e);
	        }
		}
	}
	
	private static Future<?> groupShutdown = null;
	
	public static void startDestroy() {
		synchronized(lastMessageTime) {
			if (group != null) {
				groupShutdown = group.shutdownGracefully();
			}
		}
	}
	
	public static void waitDestroy() {
		synchronized(lastMessageTime) {
			if (group != null) {
		        try {
		        	groupShutdown.sync();
		            Logger.log("Twitch pubsub Group shut down");
				} catch (InterruptedException e) {
					Logger.error(e);
				}
		        group = null;
		        groupShutdown = null;
			}
		}
	}
	
//	public static class SubGroup {
//		private static final Map<String, SubGroup> groups = new HashMap<String, SubGroup>();
//		
//		private Instant lastReceived;
//		private final String from;
//		private final List<String> recipients = new ArrayList<String>();
//		private final int tier;
//		private final List<TwitchSub> events = new ArrayList<TwitchSub>();
//		
//		private SubGroup(String from, int tier) {
//			this.from = from;
//			this.tier = tier;
//			lastReceived = Instant.now();
//		}
//		
//		private void addtoGroup(TwitchSub event) {
//			this.events.add(event);
//			this.recipients.add(event.recipient);
//			lastReceived = Instant.now();
//		}
//		
//		private void remove() {
//			groups.remove(this.from);
//			int months = 0;
//			
//			for (TwitchSub event : this.events) {
//				months += event.cumulativeMonths;
//				if (this.events.size() > 1) {
//					event.inGroup = true;
//				} else {
//					event.inGroup = false;
//				}
//				event.mod = HTTPRequests.isModerator(event.recipientID);
//				event.vip = HTTPRequests.isVIP(event.recipientID);
//	            IntegrationManager.handleEvent(event, false);
//			}
//			if (this.events.size() > 1) {//Don't count single subs as groups
//				TwitchSubGroup group = new TwitchSubGroup(Instant.now(), this.from, this.events.size(), this.recipients, this.tier, months);
//	            IntegrationManager.handleEvent(group, false);
//				Logger.log("handle group sub " + group.cumulativeMonths + " " + group.tier + " " + group.sender + " " + group.count);
//			}
//		}
//		
//		public static void add(TwitchSub event) {
//			synchronized(groups) {
//				if (!groups.containsKey(event.sender)) {
//					groups.put(event.sender(), new SubGroup(event.sender, event.tier));
//				}
//				SubGroup group = groups.get(event.sender);
//				
//				if (group.from.equals(event.sender) && group.tier == event.tier) {
//					group.addtoGroup(event);
//				} else {
//					group.remove();
//					groups.put(event.sender(), new SubGroup(event.sender, event.tier));
//					group = groups.get(event.sender);
//					group.addtoGroup(event);
//				}
//			}
//		}
//		
//		public static void test() {
//			synchronized(groups) {
//				List<SubGroup> toRemove = new ArrayList<SubGroup>();
//				
//				for (Entry<String, SubGroup> entry : groups.entrySet()) {
//					long millisSince = entry.getValue().lastReceived.until(Instant.now(), ChronoUnit.MILLIS);
//					
//					if (millisSince >= 100) {
//						toRemove.add(entry.getValue());
//					}
//				}
//				for (SubGroup group : toRemove) {
//					group.remove();
//				}
//			}
//		}
//	}
}
