/*
 * Logger class for Freemail
 *
 * this is a first attempt at fixing the logging so that not everything
 * is written to stdout. This class attempts to mimic the Logger class
 * from Freenet and calls the Freenet Logger class is available.
 *
 */

package freemail.utils;

import java.lang.NoClassDefFoundError;

public class Logger {

	static final private int INTERNAL=1;
	static final private int DEBUG=2;
	static final private int MINOR=4;
	static final private int NORMAL=8;
	static final private int ERROR=16;
	
	static boolean initialized=false;
	static boolean foundFreenetLogger=false;
	
	//static final private int loglevel=INTERNAL|DEBUG|MINOR|NORMAL|ERROR; // everything
	//static final private int loglevel=DEBUG|NORMAL|ERROR;
	static final private int loglevel=NORMAL|ERROR; // should be ok for normal users

	static private boolean useFreenetLogger()
	{
		if(!initialized) {
			try {
				freenet.support.Logger.shouldLog(0, null);
				foundFreenetLogger=true;
			}
			catch(NoClassDefFoundError ex) {
				foundFreenetLogger=false;
			}
			initialized=true;
		}
		return foundFreenetLogger;
	}
	
	static private void log(int l, Object o, String s, String level) {
		if((l&loglevel)!=0) {
			System.out.println(level+"("+o.getClass().getName()+"): "+s);
		}
	}

	static private void log(int l, Class c, String s, String level) {
		if((l&loglevel)!=0) {
			System.out.println(level+"("+c.getName()+"): "+s);
		}
	}

	static public void minor(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.minor(o,s);
		} else {
			log(MINOR,o,s,"MINOR");
		}
	}

	static public void minor(Class c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.minor(c,s);
		} else {
			log(MINOR,c,s,"MINOR");
		}
	}

	static public void normal(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.normal(o,s);
		} else {
			log(NORMAL,o,s,"NORMAL");
		}
	}

	static public void normal(Class c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.normal(c,s);
		} else {
			log(NORMAL,c,s,"NORMAL");
		}
	}

	static public void error(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.error(o,s);
		} else {
			log(ERROR,o,s,"ERROR");
		}
	}

	static public void error(Class c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.error(c,s);
		} else {
			log(ERROR,c,s,"ERROR");
		}
	}

	static public void debug(Object o, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.debug(o,s);
		} else {
			log(DEBUG,o,s,"DEBUG");
		}
	}

	static public void debug(Class c, String s) {
		if(useFreenetLogger()) {
			freenet.support.Logger.debug(c,s);
		} else {
			log(DEBUG,c,s,"DEBUG");
		}
	}

}
