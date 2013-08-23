/*
 * AccountManager.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2008 Alexander Lehmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.freenetproject.freemail;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.archive.util.Base32;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Hex;
import org.freenetproject.freemail.utils.EmailAddress;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.PropsFile;
import org.freenetproject.freemail.wot.OwnIdentity;

import freenet.support.Base64;

public class AccountManager {
	static final String ACCOUNT_FILE = "accprops";
	private static final int RTS_KEY_LENGTH = 32;

	private static final int ASYM_KEY_MODULUS_LENGTH = 4096;
	private static final BigInteger ASYM_KEY_EXPONENT = new BigInteger("17", 10);
	private static final int ASYM_KEY_CERTAINTY = 80;

	public static final String MAILSITE_SUFFIX = "mailsite";
	public static final String MAILSITE_VERSION = "-1";

	// We keep FreemailAccount objects for all the accounts in this instance of Freemail - they need to be in memory
	// anyway since there's SingleAccountWatcher thread running for each of them anyway - and we return the same object
	// each time a request is made for a given account.
	private Map<String, FreemailAccount> accounts = new HashMap<String, FreemailAccount>();

	//singleAccountWatcherList locks both these lists
	private final ArrayList<SingleAccountWatcher> singleAccountWatcherList = new ArrayList<SingleAccountWatcher>();
	private final ArrayList<Thread> singleAccountWatcherThreadList = new ArrayList<Thread>();

	private final File datadir;
	private final Freemail freemail;

	public AccountManager(File _datadir, Freemail freemail) {
		datadir = _datadir;
		if(!datadir.exists()) {
			datadir.mkdir();
		}

		this.freemail = freemail;

		for(File accountDir : datadir.listFiles()) {
			if(!accountDir.isDirectory()) {
				continue;
			}

			PropsFile accFile = getAccountFile(accountDir);
			if(accFile == null) {
				Logger.error(this, "Couldn't initialise account from directory '"+accountDir.getName()+"' - ignoring.");
				continue;
			}

			String identityId = Base64.encode(Base32.decode(accountDir.getName()));
			FreemailAccount account = new FreemailAccount(identityId, accountDir, accFile, freemail);
			account.setNickname(accFile.get("nickname"));
			synchronized(accounts) {
				accounts.put(account.getIdentity(), account);
			}
		}
	}

	public void startTasks() {
		synchronized(accounts) {
			for(FreemailAccount account : accounts.values()) {
				//Start the tasks needed for this account
				account.startTasks();

				//Now start a SingleAccountWatcher for this account
				SingleAccountWatcher saw = new SingleAccountWatcher(account, freemail);
				Thread t = new Thread(saw, "Freemail Account Watcher for "+account.getIdentity());
				t.setDaemon(true);
				t.start();

				synchronized(singleAccountWatcherList) {
					singleAccountWatcherList.add(saw);
					singleAccountWatcherThreadList.add(t);
				}
			}
		}
	}

	public FreemailAccount getAccount(String username) {
		synchronized(accounts) {
			return accounts.get(username);
		}
	}

	public List<FreemailAccount> getAllAccounts() {
		synchronized(accounts) {
			return new LinkedList<FreemailAccount>(accounts.values());
		}
	}

	public static void changePassword(FreemailAccount account, String newpassword) {
		MD5Digest md5 = new MD5Digest();

		try {
			byte[] passwordBytes = newpassword.getBytes("UTF-8");
			md5.update(passwordBytes, 0, passwordBytes.length);
		} catch (UnsupportedEncodingException e) {
			//JVMs are required to support UTF-8, so we can assume it is always available
			throw new AssertionError("JVM doesn't support UTF-8 charset");
		}
		byte[] md5passwd = new byte[md5.getDigestSize()];
		md5.doFinal(md5passwd, 0);
		String strmd5 = new String(Hex.encode(md5passwd));

		account.getProps().put("md5passwd", strmd5);
	}

	private static PropsFile getAccountFile(File accdir) {
		PropsFile accfile = PropsFile.createPropsFile(new File(accdir, ACCOUNT_FILE));

		if(!accdir.exists() || !accfile.exists()) {
			return null;
		}

		return accfile;
	}

	public static RSAKeyParameters getPrivateKey(PropsFile props) {
		String mod_str = props.get("asymkey.modulus");
		String privexp_str = props.get("asymkey.privexponent");

		if(mod_str == null || privexp_str == null) {
			Logger.error(AccountManager.class, "Couldn't get private key - account file corrupt?");
			return null;
		}

		return new RSAKeyParameters(true, new BigInteger(mod_str, 32), new BigInteger(privexp_str, 32));
	}

	private static boolean initAccFile(PropsFile accfile, OwnIdentity oid) {
		//Initialise RTS KSK
		// Use a secure RNG for this too.
		SecureRandom rnd = Freemail.getRNG();
		String rtskey = new String();

		int i;
		for(i = 0; i < RTS_KEY_LENGTH; i++) {
			rtskey += (char)(rnd.nextInt(26) + 'a');
		}

		if(!accfile.put("rtskey", rtskey)) {
			Logger.error(AccountManager.class, "Couldn't put rts key");
			return false;
		}

		// generate an RSA keypair
		Logger.normal(AccountManager.class, "Generating cryptographic keypair (this could take a few minutes)...");

		RSAKeyGenerationParameters kparams = new RSAKeyGenerationParameters(ASYM_KEY_EXPONENT, Freemail.getRNG(), ASYM_KEY_MODULUS_LENGTH, ASYM_KEY_CERTAINTY);

		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
		kpg.init(kparams);

		AsymmetricCipherKeyPair keypair = kpg.generateKeyPair();
		RSAKeyParameters pub = (RSAKeyParameters) keypair.getPublic();
		RSAKeyParameters priv = (RSAKeyParameters) keypair.getPrivate();

		accfile.put("asymkey.modulus", pub.getModulus().toString(32));
		accfile.put("asymkey.pubexponent", pub.getExponent().toString(32));
		accfile.put("asymkey.privexponent", priv.getExponent().toString(32));

		String privateKey = oid.getInsertURI();
		privateKey = privateKey.substring(0, privateKey.indexOf("/"));
		privateKey = privateKey + "/mailsite/";
		accfile.put("mailsite.privkey", privateKey);

		Logger.normal(AccountManager.class, "Account creation completed.");
		return true;
	}

	public FreemailAccount authenticate(String username, String password) {
		FreemailAccount account = null;
		synchronized(accounts) {
			account = accounts.get(username);
		}
		if(account == null) return null;

		String realmd5str = account.getProps().get("md5passwd");
		if(realmd5str == null) return null;

		MD5Digest md5 = new MD5Digest();
		try {
			md5.update(password.getBytes("UTF-8"), 0, password.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			//JVMs are required to support UTF-8, so we can assume it is always available
			throw new AssertionError("JVM doesn't support UTF-8 charset");
		}
		byte[] givenmd5 = new byte[md5.getDigestSize()];
		md5.doFinal(givenmd5, 0);

		String givenmd5str = new String(Hex.encode(givenmd5));

		if(realmd5str.equals(givenmd5str)) {
			return account;
		}
		return null;
	}

	private static void putWelcomeMessage(FreemailAccount account, EmailAddress to) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ROOT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		MailMessage m = account.getMessageBank().createMessage();
		Date currentDate = new Date();

		m.addHeader("From", "Martin Nyhus <zidel@b5zswai7ybkmvcrfddlz5euw3ifzn5z5m3bzdgpucb26mzqvsflq.freemail>");
		m.addHeader("To", to.toString());
		m.addHeader("Subject", "Welcome to Freemail!");
		m.addHeader("Date", sdf.format(currentDate));
		m.addHeader("Content-Type", "text/plain;charset=\"us-ascii\"");
		m.addHeader("Content-Transfer-Encoding", "7bit");
		m.addHeader("Content-Disposition", "inline");
		m.addHeader("Message-id", "<freemail-welcome-msg@b5zswai7ybkmvcrfddlz5euw3ifzn5z5m3bzdgpucb26mzqvsflq.freemail>");

		PrintStream ps = m.writeHeadersAndGetStream();

		ps.println("Welcome to Freemail!");
		ps.println("");
		ps.println("Thanks for downloading and testing Freemail. You can get started and send me a Freemail now by hitting 'reply'.");
		ps.println("Your new Freemail address is:");
		ps.println("");
		ps.println(to);
		ps.println("");
		ps.println("There are several places to discuss Freemail, report problems, or ask for help:");
		ps.println(" * The 'freemail' board on FMS");
		ps.println(" * The 'eng.freenet.freemail' board on Freetalk");
		ps.println(" * The bug tracker: https://bugs.freenetproject.org/ (select 'Freemail' in the top right).");
		ps.println(" * #freenet on irc.freenode.net");
		ps.println(" * The mailing list at http://emu.freenetproject.org/cgi-bin/mailman/listinfo/freemail.");
		ps.println("");
		ps.println("Don't forget to stay up to date with the Freemail news and latest version at the freesite, which can be found at:");
		ps.println("");
		ps.println("USK@M0d8y6YoLpXOeQGxu0-IDg8sE5Yt~Ky6t~GPyyZe~zo,KlqIjAj3~dA1Zf57VDljkmp3vHUozndpxnH-P2RRugI,AQACAAE/freemail/-8/");
		ps.println("");
		ps.println("Happy Freemailing!");
		ps.println("");
		ps.println("");
		ps.println("");
		ps.println("");
		ps.println("The Freemail developers");

		m.commit();
	}

	public void addIdentities(List<OwnIdentity> oids) {
		for(OwnIdentity oid : oids) {
			addIdentity(oid);
		}
	}

	private void addIdentity(OwnIdentity oid) {
		File accountDir = new File(datadir, oid.getBase32IdentityID());
		FreemailAccount account = null;
		PropsFile accProps = null;

		if(!accountDir.exists()) {
			//Need to create a new account
			if(!accountDir.mkdir()) {
				//Account creation failed, so don't start it and try again on next startup
				//FIXME: Need better error handling
				return;
			}

			accProps = PropsFile.createPropsFile(new File(accountDir, ACCOUNT_FILE));
			initAccFile(accProps, oid);

			account = new FreemailAccount(oid.getIdentityID(), accountDir, accProps, freemail);
			account.setNickname(oid.getNickname());

			String local = EmailAddress.cleanLocalPart(oid.getNickname());
			if(local.length() == 0) {
				local = "mail";
			}

			try {
				putWelcomeMessage(account, new EmailAddress(local + "@" + account.getDomain()));
			} catch (IOException e) {
				//FIXME: Handle this properly
				Logger.error(this, "Failed while sending welcome message to " + oid);
			}
		} else {
			accProps = PropsFile.createPropsFile(new File(accountDir, ACCOUNT_FILE));
			account = new FreemailAccount(oid.getIdentityID(), accountDir, accProps, freemail);
			account.setNickname(oid.getNickname());
		}

		accounts.put(account.getIdentity(), account);

		//Now start a SingleAccountWatcher for this account
		SingleAccountWatcher saw = new SingleAccountWatcher(account, freemail);
		Thread t = new Thread(saw, "Freemail Account Watcher for "+account.getIdentity());
		t.setDaemon(true);
		t.start();

		synchronized(singleAccountWatcherList) {
			singleAccountWatcherList.add(saw);
			singleAccountWatcherThreadList.add(t);
		}
	}

	void terminate() {
		synchronized(singleAccountWatcherList) {
			Iterator<SingleAccountWatcher> sawIt = singleAccountWatcherList.iterator();
			while(sawIt.hasNext()) {
				sawIt.next().kill();
				sawIt.remove();
			}

			Iterator<Thread> threadIt = singleAccountWatcherThreadList.iterator();
			while(threadIt.hasNext()) {
				threadIt.next().interrupt();
			}

			threadIt = singleAccountWatcherThreadList.iterator();
			while(threadIt.hasNext()) {
				try {
					threadIt.next().join();
				} catch (InterruptedException e) {
					Logger.error(this, "Got InterruptedException while joining SingleAccountWatcher thread");
				}
				threadIt.remove();
			}
		}
	}
}
