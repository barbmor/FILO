package pl.opole.uni.cs.unifDL.Filo.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.GregorianCalendar;

/**
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 * 
 */

public class FiloLogger {

	private static Logger logger;
	private static Level level = Level.INFO;

	public static void setLevel(String newLevel) {
		level = Level.parse(newLevel);
		if (logger != null) {
			logger.setLevel(level);
		}
	}

	private FiloLogger() throws IOException {
		logger = Logger.getLogger(FiloLogger.class.getName());
		logger.setLevel(level);
		int limit = 1000000 * 1000;
		FileHandler fileHandler = new FileHandler("filoLog.txt", limit, 1, true);

		fileHandler.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(record.getMillis());
				return record.getLevel() + " " + logTime.format(cal.getTime()) + " || " + record.getMessage() + "\n";
			}
		});
		logger.addHandler(fileHandler);
	}

	private static Logger getLogger() {
		if (logger == null) {
			try {
				new FiloLogger();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logger;
	}

	public static void log(Level level, String msg) {
		getLogger().log(level, msg);
	}
}
