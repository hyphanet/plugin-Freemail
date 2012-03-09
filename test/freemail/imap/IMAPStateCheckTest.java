package freemail.imap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IMAPStateCheckTest extends IMAPTestBase {
	/* Append is valid in authenticated and selected states */
	public void testAppend() throws IOException {
		assertFailsWithoutLogin("APPEND arg1 arg2");
	}

	/* Check is only valid in selected state */
	public void testCheck() throws IOException {
		assertFailsWithoutLogin("CHECK");
		assertFailsWithoutSelect("CHECK");
	}

	/* Close is only valid in selected state */
	public void testClose() throws IOException {
		assertFailsWithoutLogin("CLOSE");
		assertFailsWithoutSelect("CLOSE");
	}

	/* Copy is only valid in selected state */
	public void testCopy() throws IOException {
		assertFailsWithoutLogin("COPY");
		assertFailsWithoutSelect("COPY");
	}

	/* Create is valid in authenticated and selected states */
	public void testCreate() throws IOException {
		assertFailsWithoutLogin("CREATE");
	}

	/* Delete is valid in authenticated and selected states */
	public void testDelete() throws IOException {
		assertFailsWithoutLogin("DELETE");
	}

	/* Expunge is valid in selected state */
	public void testExpunge() throws IOException {
		assertFailsWithoutLogin("EXPUNGE");
		assertFailsWithoutSelect("EXPUNGE");
	}

	/* Fetch is valid in selected state */
	public void testFetch() throws IOException {
		assertFailsWithoutLogin("FETCH");
		assertFailsWithoutSelect("FETCH");
	}

	/* List is valid in authenticated and selected states */
	public void testList() throws IOException {
		assertFailsWithoutLogin("LIST");
	}

	/* Lsub is valid in authenticated and selected states */
	public void testLsub() throws IOException {
		assertFailsWithoutLogin("LSUB");
	}

	/* Namespace is valid in authenticated and selected states */
	public void testNamespace() throws IOException {
		assertFailsWithoutLogin("NAMESPACE");
	}

	/* Select is valid in authenticated and selected states */
	public void testSelect() throws IOException {
		assertFailsWithoutLogin("SELECT");
	}

	/* Status is valid in authenticated and selected states */
	public void testStatus() throws IOException {
		assertFailsWithoutLogin("STATUS");
	}

	/* Store is valid in selected state */
	public void testStore() throws IOException {
		assertFailsWithoutLogin("STORE arg1 arg2");
		assertFailsWithoutSelect("STORE arg1 arg2");
	}

	/* UID is valid in selected state */
	public void testUid() throws IOException {
		assertFailsWithoutLogin("UID arg1 arg2 arg3");
		assertFailsWithoutSelect("UID arg1 arg2 arg3");
	}

	private void assertFailsWithoutLogin(String cmd) throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 " + cmd);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 NO Must be authenticated");

		runSimpleTest(commands, expectedResponse);
	}

	private void assertFailsWithoutSelect(String cmd) throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 " + cmd);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("0002 NO No mailbox selected");

		runSimpleTest(commands, expectedResponse);
	}
}
