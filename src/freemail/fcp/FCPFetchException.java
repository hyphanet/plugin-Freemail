package freemail.fcp;

import freemail.utils.Logger;

public class FCPFetchException extends Exception {
	static final long serialVersionUID = -1;
	
	// The following code shamelessly stolen from Freenet's FetchException.java (but reordered)
	/** Too many levels of recursion into archives */
	public static final int TOO_DEEP_ARCHIVE_RECURSION = 1;
	/** Don't know what to do with splitfile */
	public static final int UNKNOWN_SPLITFILE_METADATA = 2;
	/** Don't know what to do with metadata */
	public static final int UNKNOWN_METADATA = 3;
	/** Got a MetadataParseException */
	public static final int INVALID_METADATA = 4;
	/** Got an ArchiveFailureException */
	public static final int ARCHIVE_FAILURE = 5;
	/** Failed to decode a block */
	public static final int BLOCK_DECODE_ERROR = 6;
	/** Too many split metadata levels */
	public static final int TOO_MANY_METADATA_LEVELS = 7;
	/** Too many archive restarts */
	public static final int TOO_MANY_ARCHIVE_RESTARTS = 8;
	/** Too deep recursion */
	public static final int TOO_MUCH_RECURSION = 9;
	/** Tried to access an archive file but not in an archive */
	public static final int NOT_IN_ARCHIVE = 10;
	/** Too many meta strings. E.g. requesting CHK@blah,blah,blah as CHK@blah,blah,blah/filename.ext */
	public static final int TOO_MANY_PATH_COMPONENTS = 11;
	/** Failed to read from or write to a bucket; a kind of internal error */
	public static final int BUCKET_ERROR = 12;
	/** Data not found */
	public static final int DATA_NOT_FOUND = 13;
	/** Route not found */
	public static final int ROUTE_NOT_FOUND = 14;
	/** Downstream overload */
	public static final int REJECTED_OVERLOAD = 15;
	/** Too many redirects */
	public static final int TOO_MANY_REDIRECTS = 16;
	/** An internal error occurred */
	public static final int INTERNAL_ERROR = 17;
	/** The node found the data but the transfer failed */
	public static final int TRANSFER_FAILED = 18;
	/** Splitfile error. This should be a SplitFetchException. */
	public static final int SPLITFILE_ERROR = 19;
	/** Invalid URI. */
	public static final int INVALID_URI = 20;
	/** Too big */
	public static final int TOO_BIG = 21;
	/** Metadata too big */
	public static final int TOO_BIG_METADATA = 22;
	/** Splitfile has too big segments */
	public static final int TOO_MANY_BLOCKS_PER_SEGMENT = 23;
	/** Not enough meta strings in URI given and no default document */
	public static final int NOT_ENOUGH_PATH_COMPONENTS = 24;
	/** Explicitly cancelled */
	public static final int CANCELLED = 25;
	/** Archive restart */
	public static final int ARCHIVE_RESTART = 26;
	/** There is a more recent version of the USK, ~= HTTP 301; FProxy will turn this into a 301 */
	public static final int PERMANENT_REDIRECT = 27;
	/** Not all data was found; some DNFs but some successes */
	public static final int ALL_DATA_NOT_FOUND = 28;
	/** Requestor specified a list of allowed MIME types, and the key's type wasn't in the list */
	public static final int WRONG_MIME_TYPE = 29;
	/** A node killed the request because it had recently been tried and had DNFed */
	public static final int RECENTLY_FAILED = 30;
	
	private final FCPMessage fcpMessage;
	
	public FCPFetchException(FCPMessage fcpmsg) {
		fcpMessage = fcpmsg;
	}
	
	public FCPMessage getFailureMessage() {
		return fcpMessage;
	}
	
	public int getCode() {
		String strCode = (String)fcpMessage.headers.get("Code");
		if (strCode == null) {
			Logger.error(this, "FCP error message with no error code!");
			return 0;
		}
		return Integer.parseInt(strCode);
	}
	
	public String getMessage() {
		String msg = (String)fcpMessage.headers.get("ShortCodeDescription");
		if (msg != null) return msg;
		
		// No short description? try the long one.
		msg = (String)fcpMessage.headers.get("CodeDescription");
		if (msg != null) return msg;
		
		// No? Does it have a code?
		msg = (String)fcpMessage.headers.get("Code");
		if (msg != null) return "Error number "+msg+" (no description given)";
		
		// Give up then
		return "Unknown error (no hints given by the node)";
	}
	
	/**
	 * @return true if the error is the fault of the network or our connection to it, and has no bearing on whether or not
	 *              the key itself is present or not.
	 */
	public boolean isNetworkError() {
		switch (getCode()) {
			case BUCKET_ERROR:
			case ROUTE_NOT_FOUND:
			case REJECTED_OVERLOAD:
			case INTERNAL_ERROR:
			case TRANSFER_FAILED:
			case CANCELLED:
				return true;
		}
		return false;
	}
	
	/**
	 * @return true if all future requests for this this key will fail too
	 */
	public boolean isFatal() {
		String fatal = (String)fcpMessage.headers.get("Fatal");
		if (fatal == null) {
			Logger.error(this, "No 'fatal' field found - FCP protocol change? Assuming not fatal.");
			return false;
		}
		return fatal.equalsIgnoreCase("true");
	}
}
