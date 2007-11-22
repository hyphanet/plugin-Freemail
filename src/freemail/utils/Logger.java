/*
 * Logger class for Freemail
 *
 * this is a first attempt at fixing the logging so that not everything
 * is written to stdout. This class attempts to mimic the Logger class
 * from Freenet, later we can probably use the class from Freenet without
 * changing much except the import statement.
 *
 */

package freemail.utils;

public class Logger {

	static final private int INTERNAL=1;
	static final private int DEBUG=2;
	static final private int MINOR=4;
	static final private int NORMAL=8;
	static final private int ERROR=16;
	
	//static final private int loglevel=INTERNAL|DEBUG|MINOR|NORMAL|ERROR; // everything
	//static final private int loglevel=DEBUG|NORMAL|ERROR;
	static final private int loglevel=NORMAL|ERROR; // should be ok for normal users

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
		log(MINOR,o,s,"MINOR");
	}

	static public void minor(Class c, String s) {
		log(MINOR,c,s,"MINOR");
	}

	static public void normal(Object o, String s) {
		log(NORMAL,o,s,"NORMAL");
	}

	static public void normal(Class c, String s) {
		log(NORMAL,c,s,"NORMAL");
	}

	static public void error(Object o, String s) {
		log(ERROR,o,s,"ERROR");
	}

	static public void error(Class c, String s) {
		log(ERROR,c,s,"ERROR");
	}

	static public void debug(Object o, String s) {
		log(DEBUG,o,s,"DEBUG");
	}

	static public void debug(Class c, String s) {
		log(DEBUG,c,s,"DEBUG");
	}

}
