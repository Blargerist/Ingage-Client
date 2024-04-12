package ingage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import ingage.connection.IntegrationSocketServer;
import ingage.connection.IntegrationWebSocketServer;
import ingage.connection.TwitchEventSub;

public class ThreadManager {

	public static Thread twitchEventSubTimerThread;
	public static Thread integrationServerQueueThread;
	private static ExecutorService executor;
	
	public static void initTwitchEventSubTimerThread() {
		twitchEventSubTimerThread = new Thread(null, new Runnable() {
			@Override
			public void run() {
				while (!IngageClient.shutdown) {
					//If it's been longer than 30 seconds, restart connection
		            if (TwitchEventSub.lastMessageTime.until(Instant.now(), ChronoUnit.SECONDS) > 20) {
		            	TwitchEventSub.init();
		            }
		            try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						
					}
		        }
			}
		}, "Twitch EventSub Timers");
		
		twitchEventSubTimerThread.start();
	}
	
	public static void shutdownTwitchEventSubTimerThread() {
		if (twitchEventSubTimerThread != null) {
			try {
				twitchEventSubTimerThread.join(5000);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
			if (twitchEventSubTimerThread.isAlive()) {
				twitchEventSubTimerThread.stop();
			}
			twitchEventSubTimerThread = null;
		}
	}
	
	public static void initIntegrationServerQueueThread() {
		integrationServerQueueThread = new Thread(null, new Runnable() {
			@Override
			public void run() {
				while (!IngageClient.shutdown) {
					IntegrationWebSocketServer.tick();
					IntegrationSocketServer.tick();
		            try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						
					}
		        }
			}
		}, "Integration Server Tick Thread");
		
		integrationServerQueueThread.start();
	}
	
	public static void shutdownIntegrationServerQueueThread() {
		if (integrationServerQueueThread != null) {
			try {
				integrationServerQueueThread.join(5000);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
			if (integrationServerQueueThread.isAlive()) {
				integrationServerQueueThread.stop();
			}
			integrationServerQueueThread = null;
		}
	}
	
	public static void execute(Callable<Object> callable) {
		execute(Arrays.asList(callable));
	}
	
	public static void execute(List<Callable<Object>> callables) {
		try {
			ThreadManager.executor.invokeAll(callables);
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}
	
	public static void initExecutor() {
		ThreadManager.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
			
			private int counter = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setName("Ingage-Worker-Thread-" + counter++);
				return thread;
			}
			
		});
	}
	
	public static void shutdownExecutor() {
		if (ThreadManager.executor != null)
        {
			ThreadManager.executor.shutdown();
            try {
            	ThreadManager.executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
        }
	}
}
