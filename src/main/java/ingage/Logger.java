package ingage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

public class Logger {
	private static final Object lock = new Object();
	private static final DateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat timePrependFormat = new SimpleDateFormat("hh:mm:ss aa: ");
	public static final String LOG_FOLDER = "./logs/";
	public static final String LOG_FILE_EXTENSION = ".txt";
	private static final String LOG_FILE = LOG_FOLDER + fileNameFormat.format(new Date()) + LOG_FILE_EXTENSION;
	
	public static void log(String toLog) {
		synchronized(lock) {
			try {
				Date date = new Date();
				StringBuilder toWrite = new StringBuilder();
				toWrite.append(timePrependFormat.format(date));
				toWrite.append(toLog);
				System.out.println(toWrite);
				
				File file = new File(LOG_FILE);
				file.getParentFile().mkdirs();
				
				if (!file.createNewFile()) {
					toWrite.insert(0, System.lineSeparator());
				}
				Files.asCharSink(file, StandardCharsets.UTF_8, FileWriteMode.APPEND).write(toWrite.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void error(Throwable cause) {
		error("", cause);
	}
	
	public static void error(String toLog, Throwable cause) {
		StringBuilder toWrite = new StringBuilder();
		toWrite.append(toLog);
		toWrite.append(" : ");
		toWrite.append(cause.getLocalizedMessage());
		for (StackTraceElement trace : cause.getStackTrace()) {
			toWrite.append(System.lineSeparator());
			toWrite.append(trace.toString());
		}
		log(toWrite.toString());
		cause.printStackTrace();
	}
}
