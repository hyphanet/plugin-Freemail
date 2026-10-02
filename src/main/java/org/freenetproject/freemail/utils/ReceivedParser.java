package org.freenetproject.freemail.utils;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Basic parser for MIME “Received” headers, as they are defined in
 * <a href="https://www.rfc-editor.org/info/rfc5322/#section-3.6.7">RFC
 * 5322, § 3.6.7</a>.
 */
public class ReceivedParser {

	/**
	 * Parses the given header value as a “Received” header and returns the
	 * values it parsed from it.
	 * <p>
	 * Currently, the only relevant value parsed from the header is the date,
	 * and that is only used when the list of tokens contains a comment with
	 * the text of “Freemail”.
	 * </p>
	 *
	 * @param headerValue The “Received” header value to parse
	 * @return The parsed header values
	 */
	public Received parse(String headerValue) {
		var parts = headerValue.split(";", 2);
		if ((parts.length >= 2) && parts[0].contains("(Freemail)")) {
			var dateTime = DateStringFactory.parseFullDate(parts[1].trim());
			return new Received(dateTime);
		}
		return new Received(Optional.empty());
	}

	public record Received(Optional<OffsetDateTime> dateTime) {}

}
