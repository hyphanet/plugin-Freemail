package fnmail.fcp;

public class FCPInsertErrorMessage extends FCPErrorMessage {
	/* Caller supplied a URI we cannot use */
	public static final int INVALID_URI = 1;
	/* Failed to read from or write to a bucket; a kind of internal error */
	public static final int BUCKET_ERROR = 2;
	/* Internal error of some sort */
	public static final int INTERNAL_ERROR = 3;
	/* Downstream node was overloaded */
	public static final int REJECTED_OVERLOAD = 4;
	/* Couldn't find enough nodes to send the data to */
	public static final int ROUTE_NOT_FOUND = 5;
	/* There were fatal errors in a splitfile insert. */
	public static final int FATAL_ERRORS_IN_BLOCKS = 6;
	/* Could not insert a splitfile because a block failed too many times */
	public static final int TOO_MANY_RETRIES_IN_BLOCKS = 7;
	/* Not able to leave the node at all */
	public static final int ROUTE_REALLY_NOT_FOUND = 8;
	/* Collided with pre-existing content */
	public static final int COLLISION = 9;
	/* Cancelled by user */
	public static final int CANCELLED = 10;

	FCPInsertErrorMessage(FCPMessage msg) {
		super(msg);
	}
}
