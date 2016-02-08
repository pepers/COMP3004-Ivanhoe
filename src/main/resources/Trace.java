/*
 * Writes log files for us.
 */
package main.resources;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.sql.SQLException;
import java.text.*;
import java.util.Date;

public class Trace {
	final static Logger clientLogger = Logger.getLogger("client");
	final static Logger serverLogger = Logger.getLogger("server");
	final static Logger logger = Logger.getLogger("FILE");
	   
	public static void main(String[] args) throws IOException, SQLException{
		PropertyConfigurator.configure("logs/log4j.properties");
	}
	
	public static void exception(Object o, Exception e) {
		String message = String.format("Exception thrown: %s \n", e.getMessage());
		write(o, message);
	}
	
	public static void write(Object o, String message) {
		switch (o.getClass().getSimpleName()) {
			case "Client":
				clientLogger.info(format(o,message));
			case "Server":
				serverLogger.info(format(o,message));
			default:
				logger.info(format(o,message));				
		}
	}
	
	private static String format (Object o, String message) {
		return String.format ("[Time: %23s] Class: %-12s: %s", getDateTime(), o.getClass().getSimpleName(), message) ;
	}
	
	private static String getDateTime () {
		Format formatter = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss:SSS");
		return formatter.format((new Date()).getTime());
	}
}
