package ingage.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ingage.Logger;
import ingage.Util;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.concurrent.Future;

public class IntegrationWebSocketServer {
	private static EventLoopGroup bossGroup = null;
	private static EventLoopGroup workerGroup = null;
	private static final ConcurrentHashMap<String, ChannelHandlerContext> channels = new ConcurrentHashMap<String, ChannelHandlerContext>();
	private static final ConcurrentHashMap<String, List<String>> integrations = new ConcurrentHashMap<String, List<String>>();
	private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<IntegrationMessage>> queues = new ConcurrentHashMap<String, ConcurrentLinkedQueue<IntegrationMessage>>();
	
	public static void queueIntegrationMessage(String integrationID, JsonObject toSend) {
		for (Entry<String, ConcurrentLinkedQueue<IntegrationMessage>> entry : IntegrationWebSocketServer.queues.entrySet()) {
			entry.getValue().add(new IntegrationMessage(integrationID, toSend));
		}
	}
	
	public static void tick() {
		for (Entry<String, ChannelHandlerContext> entry : IntegrationWebSocketServer.channels.entrySet()) {
			try {
				ConcurrentLinkedQueue<IntegrationMessage> queue = queues.get(entry.getKey());
				
				if (queue != null) {
					if (!queue.isEmpty()) {
						entry.getValue().writeAndFlush(new TextWebSocketFrame(queue.poll().toSend.toString())).await();
					}
				}
			} catch (Exception e) {
				
			}
		}
	}
	
	public static void init() {
        try {
    		startDestroy();
    		waitDestroy();
    		int port = 23491;
    		//Accepts connection
    		bossGroup = new NioEventLoopGroup(1);
    		//Handles traffic
            workerGroup = new NioEventLoopGroup();
        	//Helper class to build server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new Initializer());
    
            //Start server
            ChannelFuture f = b.bind(port).sync();
            Logger.log("Integration Server Websocket bound on " + port);
        } catch (InterruptedException e) {
			Logger.error(e);
		}
	}
	
	private static Future<?> workerShutdown = null;
	private static Future<?> bossShutdown = null;
	
	public static void startDestroy() {
		if (workerGroup != null) {
			workerShutdown = workerGroup.shutdownGracefully();
		}
		if (bossGroup != null) {
			bossShutdown = bossGroup.shutdownGracefully();
		}
	}
	
	public static void waitDestroy() {
		if (workerGroup != null) {
	        try {
	        	workerShutdown.sync();
	            Logger.log("Integration Server WorkerGroup shut down");
			} catch (InterruptedException e) {
				Logger.error(e);
			}
	        workerGroup = null;
	        workerShutdown = null;
		}
		if (bossGroup != null) {
			try {
				bossShutdown.sync();
	            Logger.log("Integration Server bossGroup shut down");
			} catch (InterruptedException e) {
				Logger.error(e);
			}
			bossGroup = null;
			bossShutdown = null;
		}
	}
	
	private static class Initializer extends ChannelInitializer<SocketChannel> {
		
		private static final String WEBSOCKET_PATH = "/ws";
		
		public Initializer() {
			
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new HttpServerCodec());
			ch.pipeline().addLast(new HttpObjectAggregator(65536));
			//Handle upgrade to socket
			ch.pipeline().addLast(new WebsocketUpgradeHandler());
			ch.pipeline().addLast(new WebSocketServerCompressionHandler());
			ch.pipeline().addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
			ch.pipeline().addLast(new FrameHandler());
		}
	}
	
	private static class WebsocketUpgradeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
			//Make sure the decode succeeded
			if (!msg.decoderResult().isSuccess()) {
				sendErrorResponse(ctx, new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.BAD_REQUEST, ctx.alloc().buffer(0)));
				return;
			}
			//Handle socket upgrade requests
			if (msg.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
				ctx.fireChannelRead(msg.retain());
				return;
			}
			//Other requests
			sendErrorResponse(ctx, new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.NOT_FOUND, ctx.alloc().buffer(0)));
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			Logger.error(cause);
			ctx.close();
		}

		//Send response and close channel
	    private static void sendErrorResponse(ChannelHandlerContext ctx, FullHttpResponse res) {
            ByteBufUtil.writeUtf8(res.content(), res.status().toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
            
	        HttpUtil.setKeepAlive(res, false);
	        
	        ChannelFuture future = ctx.writeAndFlush(res);
            future.addListener(ChannelFutureListener.CLOSE);
	    }
	}
	
	private static class FrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
		
		@Override
		public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
			if (msg instanceof TextWebSocketFrame) {
				String str = ((TextWebSocketFrame)msg).text();
				
				try {
					JsonObject json = Util.GSON.fromJson(str, JsonObject.class);
					
					String type = json.get("type").getAsString();
					
					if (type.equalsIgnoreCase("listen")) {
						JsonObject payload = json.get("payload").getAsJsonObject();
						
						JsonArray integrations = payload.get("integrations").getAsJsonArray();
						
						List<String> integrationList = IntegrationWebSocketServer.integrations.get(ctx.channel().id().asLongText());
						
						if (integrationList != null) {
							for (int i = 0; i < integrations.size(); i++) {
								Logger.log("Listening to: "+ integrations.get(i).getAsString());
								integrationList.add(integrations.get(i).getAsString());
							}
						}
					}
					
				} catch (Exception e) {
					Logger.error("Error reading json from integration connection", e);
				}
			}
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			IntegrationWebSocketServer.channels.put(ctx.channel().id().asLongText(), ctx);
			IntegrationWebSocketServer.integrations.put(ctx.channel().id().asLongText(), Collections.synchronizedList(new ArrayList<String>()));
			IntegrationWebSocketServer.queues.put(ctx.channel().id().asLongText(), new ConcurrentLinkedQueue<IntegrationMessage>());
			super.channelActive(ctx);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			IntegrationWebSocketServer.channels.remove(ctx.channel().id().asLongText());
			IntegrationWebSocketServer.integrations.remove(ctx.channel().id().asLongText());
			IntegrationWebSocketServer.queues.remove(ctx.channel().id().asLongText());
			super.channelInactive(ctx);
		}
		
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			//Remove the upgrade handler once the upgrade is complete
			if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
				ctx.pipeline().remove(WebsocketUpgradeHandler.class);
			} else {
				super.userEventTriggered(ctx, evt);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			Logger.error(cause);
			ctx.close();
		}
	}
}
