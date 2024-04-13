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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;

public class IntegrationSocketServer {
	private static EventLoopGroup bossGroup = null;
	private static EventLoopGroup workerGroup = null;
	private static final ConcurrentHashMap<String, ChannelHandlerContext> channels = new ConcurrentHashMap<String, ChannelHandlerContext>();
	private static final ConcurrentHashMap<String, List<String>> integrations = new ConcurrentHashMap<String, List<String>>();
	private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<IntegrationMessage>> queues = new ConcurrentHashMap<String, ConcurrentLinkedQueue<IntegrationMessage>>();
		
	public static void queueIntegrationMessage(String integrationID, JsonObject toSend) {
		for (Entry<String, ConcurrentLinkedQueue<IntegrationMessage>> entry : IntegrationSocketServer.queues.entrySet()) {
			entry.getValue().add(new IntegrationMessage(integrationID, toSend));
		}
	}
	
	public static void tick() {
		for (Entry<String, ChannelHandlerContext> entry : IntegrationSocketServer.channels.entrySet()) {
			try {
				ConcurrentLinkedQueue<IntegrationMessage> queue = queues.get(entry.getKey());
				
				if (queue != null) {
					if (!queue.isEmpty()) {
						IntegrationMessage msg = queue.poll();
						entry.getValue().writeAndFlush(msg.toSend.toString()+"\r\n").await();
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
    		int port = 23492;
    		//Accepts connection
    		bossGroup = new NioEventLoopGroup(1);
    		//Handles traffic
            workerGroup = new NioEventLoopGroup();
        	//Helper class to build server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new Initializer())
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);
    
            //Start server
            ChannelFuture f = b.bind(port).sync();
            Logger.log("Integration Server Socket bound on " + port);
        } catch (InterruptedException e) {
			Logger.error(e);
		}
	}
	
	private static Future<?> workerShutdown = null;
	private static Future<?> bossShutdown = null;
	
	public static void startDestroy() {
		for (Entry<String, ChannelHandlerContext> entry : IntegrationSocketServer.channels.entrySet()) {
			try {
				entry.getValue().writeAndFlush("{\"type\":\"CLOSE\"}\r\n").await();
			} catch (Exception e) {
				
			}
		}
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
		
		private static final StringDecoder DECODER = new StringDecoder();
		private static final StringEncoder ENCODER = new StringEncoder();
		
		public Initializer() {
			
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			//Line delimiter
			ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
			ch.pipeline().addLast(DECODER);
			ch.pipeline().addLast(ENCODER);
			ch.pipeline().addLast(new Handler());
		}
	}
	
	private static class Handler extends SimpleChannelInboundHandler<String> {
		
		@Override
		public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
			Logger.log("Integration socket: "+msg);
			
			try {
				JsonObject json = Util.GSON.fromJson(msg, JsonObject.class);
				
				String type = json.get("type").getAsString();
				
				switch (type) {
					case "LISTEN": {
						JsonObject payload = json.get("payload").getAsJsonObject();
						
						JsonArray integrations = payload.get("integrations").getAsJsonArray();
						
						List<String> integrationList = IntegrationSocketServer.integrations.get(ctx.channel().id().asLongText());
						
						if (integrationList != null) {
							for (int i = 0; i < integrations.size(); i++) {
								Logger.log("Listening to: "+ integrations.get(i).getAsString());
								integrationList.add(integrations.get(i).getAsString());
							}
						}
						break;
					}
					case "CLOSE": {
						ctx.close();
						break;
					}
				}
				
			} catch (Exception e) {
				Logger.error("Error reading json from integration connection", e);
			}
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			Logger.log("Integration Telnet Socket connection Active");
			IntegrationSocketServer.channels.put(ctx.channel().id().asLongText(), ctx);
			IntegrationSocketServer.integrations.put(ctx.channel().id().asLongText(), Collections.synchronizedList(new ArrayList<String>()));
			IntegrationSocketServer.queues.put(ctx.channel().id().asLongText(), new ConcurrentLinkedQueue<IntegrationMessage>());
			super.channelActive(ctx);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			Logger.log("Integration Telnet Socket connection Inactive");
			IntegrationSocketServer.channels.remove(ctx.channel().id().asLongText());
			IntegrationSocketServer.integrations.remove(ctx.channel().id().asLongText());
			IntegrationSocketServer.queues.remove(ctx.channel().id().asLongText());
			super.channelInactive(ctx);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			Logger.error(cause);
			ctx.close();
		}
	}
}
