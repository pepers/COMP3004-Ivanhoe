/*
 * Writes log files for us.
 */
package main.resources;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.sql.SQLException;

public class Trace {
	/* Get actual class name to be printed on */
	final static Logger log = Logger.getLogger(Trace.class.getName());
	   
	public static void main(String[] args)throws IOException,SQLException{
		PropertyConfigurator.configure("logs/log4j.properties");
		log.debug("Hello this is a debug message");
		log.info("Hello this is an info message");
	}
}
