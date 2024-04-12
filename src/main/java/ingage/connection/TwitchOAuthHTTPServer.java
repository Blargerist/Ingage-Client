package ingage.connection;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import ingage.Logger;
import ingage.Util;
import ingage.auth.AuthManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;

public class TwitchOAuthHTTPServer extends SimpleChannelInboundHandler<Object> {
	
	private static EventLoopGroup bossGroup = null;
	private static EventLoopGroup workerGroup = null;
//	private static HttpRequest request = null;

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	
//	private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//		Logger.log("Recieved something oauth " + msg.getClass().toString());
		if (msg instanceof HttpRequest) {
//			Logger.log("request");
			HttpRequest request = (HttpRequest) msg;
			
			if (HttpUtil.is100ContinueExpected(request)) {
		        writeResponse(ctx);
		    }
//			Logger.log("uri " + request.uri());
			String uri = request.uri();
			
//			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
//                    Unpooled.wrappedBuffer(CONTENT));
//            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
//            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//
//            response.headers().set(HttpHeaderNames.CONNECTION, Values.KEEP_ALIVE);
//            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//            ctx.write(response);
			
			if (uri.equals("/User/ReceiveTwitchOAuth")) {
//				Logger.log("oauth");
				String responseStr = 
						"<HTML>" +
                                "<BODY>" +
                                "<script>" +
//                                "document.body.innerHTML = document.location.hash;" +
                                "var xhr = new XMLHttpRequest();" +
//                                "var params = document.location.hash;" +
                                "xhr.open('POST', 'http://localhost:8080/twitch/oauth/fragment', true);" +
                                "xhr.setRequestHeader('Content-Type', 'application/json');" +
//                                "xhr.onload = function() {" +
//                                "document.body.innerHTML = 'test';"+
//                                "};"+
                                "xhr.send(JSON.stringify({fragment: document.location.hash}));" +
                                "window.location.href = 'http://www.google.com';" +
//                                "window.close();" +
//"fetch('http://localhost:8080/twitch/oauth/fragment?document.location.hash').then(function(response) {"+
//	  "return response.json();"+
//	"}).then(function(data) {"+
//	  "console.log(data);"+
//	"}).catch(function() {"+
//	  "console.log('Booo');"+
//	"});"+
                                "</script>" +
                                "</BODY>" +
                                "</HTML>";
				
				FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseStr, CharsetUtil.UTF_8));
				httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
				httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
				httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				httpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
				ctx.write(httpResponse);
				
			} else if (uri.equals("/twitch/oauth/fragment")) {
//				Logger.log("fragment");
//				
//				Logger.log(request.decoderResult().toString());
			}
		}
		if (msg instanceof HttpContent) {
//			Logger.log("content");
			HttpContent content = (HttpContent) msg;

//			Logger.log("uri " + content.);
//			String uri = content..uri();
			
//			if (uri.equals("/twitch/oauth/fragment")) {
//				Logger.log("content");
				
			ByteBuf cntn = content.content();
			
			if (cntn.readableBytes() > 0) {
				String str = cntn.toString(CharsetUtil.UTF_8);
				
				JsonObject json = Util.GSON.fromJson(str, JsonObject.class);
				
				if (json.has("fragment")) {
					String fragment = json.get("fragment").getAsString();
					Map<String, String> args = splitArgs(fragment);
					
					String token = args.get("access_token");

	                if (token != null) {
//						Logger.log("Token: " + token);
						
						AuthManager.onTwitchOAuth(token, true);
//						if (Util.isNullOrEmpty(AuthManager.Keys.getTwitchAuthToken()) || !AuthManager.Keys.getTwitchAuthToken().equals(token)) {
//							AuthManager.Keys.setTwitchAuthToken(token);
//							ThreadManager.runOnMainThread(() -> {
//								ConnectionManager.initPostOAuth();
//							});
//						}
						ctx.close();
	                }
				}
			}
//				content.release();
//			}
		}
	}
	
	private static Map<String, String> splitArgs(String msg) {
		Map<String, String> dict = new HashMap<String, String>();

		String[] split = msg.substring(1).split("&");

        for (String item : split) {
        	String[] split2 = item.split("=");
            dict.put(split2[0], split2[1]);
        }
        return dict;
    }
	
	private void writeResponse(ChannelHandlerContext ctx) {
	    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, 
	      Unpooled.EMPTY_BUFFER);
	    ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Logger.error(cause);
		ctx.close();
	}
	
	public static void init() {
        try {
    		startDestroy();
    		waitDestroy();
    		int port = 8080;
    		//Accepts connection
    		bossGroup = new NioEventLoopGroup();
    		//Handles traffic
            workerGroup = new NioEventLoopGroup();
        	//Helper class to build server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 ch.pipeline().addLast("decoder", new HttpServerCodec());
//                	 ch.pipeline().addLast("encoder", new HttpRequestEncoder());
                     ch.pipeline().addLast(new TwitchOAuthHTTPServer());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            Logger.log("Socket bound on " + port);
            
            //f.channel().closeFuture().sync();
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
	            Logger.log("Twitch oauth WorkerGroup shut down");
			} catch (InterruptedException e) {
				Logger.error(e);
			}
	        workerGroup = null;
	        workerShutdown = null;
		}
		if (bossGroup != null) {
			try {
				bossShutdown.sync();
	            Logger.log("Twitch oauth bossGroup shut down");
			} catch (InterruptedException e) {
				Logger.error(e);
			}
			bossGroup = null;
			bossShutdown = null;
		}
	}
}
