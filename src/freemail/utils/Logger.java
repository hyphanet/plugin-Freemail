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

	static private void log(int l, Object t, String s, String level) {
		if((l&loglevel)!=0) {
			System.out.println(level+"("+t.getClass().getSimpleName()+"): "+s);
		}
	}

	static public void minor(Object t, String s) {
		log(MINOR,t,s,"MINOR");
	}

	static public void normal(Object t, String s) {
		log(NORMAL,t,s,"NORMAL");
	}

	static public void error(Object t, String s) {
		log(ERROR,t,s,"ERROR");
	}

	static public void debug(Object t, String s) {
		log(DEBUG,t,s,"DEBUG");
	}

}
