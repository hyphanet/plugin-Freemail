package org.freenetproject.freemail.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ReceivedParserTest {

	@Test
	public void parserCanNotParseHeaderWithoutDate() {
		var received = receivedParser.parse("by freemail.hypha from Alice for Bob");
		assertThat(received.dateTime().isPresent(), equalTo(false));
	}

	@Test
	public void parserCanParseCompleteDateFromHeader() {
		var received = receivedParser.parse("(Freemail); Wed, 23 Sep 2026 08:16:55 +0200");
		assertThat(received.dateTime().get(), equalTo(OffsetDateTime.of(LocalDateTime.of(2026, 9, 23, 8, 16, 55), ZoneOffset.of("+0200"))));
	}

	@Test
	public void parserCanParseDateWithoutWeekdayFromHeader() {
		var received = receivedParser.parse("(Freemail); 23 Sep 2026 08:16:55 +0200");
		assertThat(received.dateTime().get(), equalTo(OffsetDateTime.of(LocalDateTime.of(2026, 9, 23, 8, 16, 55), ZoneOffset.of("+0200"))));
	}

	@Test
	public void parserIgnoresHeaderWithoutFreemailComment() {
		var received = receivedParser.parse("from Alice for Bob; 23 Sep 2026 08:16:55 +0200");
		assertThat(received.dateTime().isPresent(), equalTo(false));
	}

	@Test
	public void parserCanNotParseInvalidDate() {
		var received = receivedParser.parse("(Freemail); no date");
		assertThat(received.dateTime().isPresent(), equalTo(false));
	}

	private final ReceivedParser receivedParser = new ReceivedParser();

}
