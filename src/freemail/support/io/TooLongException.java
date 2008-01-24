package freemail.support.io;

/*
 * This file originates from the main Freenet distribution, originally in freenet.support.io
 */

import java.io.IOException;

/** Exception thrown by a LineReadingInputStream when a line is too long. */
public class TooLongException extends IOException {
	static final long serialVersionUID = -1;
}