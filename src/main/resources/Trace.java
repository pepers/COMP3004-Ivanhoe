/*
 * Writes log files for us.
 */
package main.resources;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.*;
import java.util.Date;
import java.util.Properties;

public class Trace {
	private static Trace _instance = null;
	final static Logger log = Logger.getLogger("FILE");
	private enum Level {DEBUG, ERROR, INFO};
	private Level level = Level.INFO;

	public Trace() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("logs/log4j.properties");
		PropertyConfigurator.configure(url);
	}
	
	public static Trace getInstance() {
		if (_instance == null) {
			synchronized (Trace.class) {
				_instance = new Trace();
			}
		}
		return _instance;
	}
	
	/*
	 *  for error logging
	 */
	public void exception (Object o, Exception e) {
		String message = String.format("Exception thrown: %s \n", e.getMessage());
		level = Level.ERROR;
		log.error(format(o, message));
	}
	
	/*
	 *  for test logging
	 */
	public void test (Object o, String message) {
		level = Level.DEBUG;
		log.debug(format(o, message));
	}
	
	/*
	 *  for normal behaviour logging
	 */
	public void write (Object o, String message) {
		level = Level.INFO;
		log.info(format(o, message));
	}
	
	private String format (Object o, String message) {
		return String.format ("[%5s] [Time: %23s] [Class: %-12s] %s", 
				level.toString(),
				getDateTime(), 
				o.getClass().getSimpleName(), 
				message) ;
	}
	
	private String getDateTime () {
		Format formatter = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss:SSS");
		return formatter.format((new Date()).getTime());
	}
}
