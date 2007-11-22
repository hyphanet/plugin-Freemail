/*
 * AccountManager.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
 */

package freemail;

import java.io.File;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.net.MalformedURLException;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Hex;

import org.archive.util.Base32;

import freemail.FreenetURI;
import freemail.fcp.ConnectionTerminatedException;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.SSKKeyPair;
import freemail.utils.PropsFile;
import freemail.utils.EmailAddress;

public class AccountManager {
	public static final String DATADIR = "data";
	// this really doesn't matter a great deal
	public static final String NIMDIR = "nim";
	
	private static final String ACCOUNT_FILE = "accprops";
	private static final int RTS_KEY_LENGTH = 32;
	
	private static final int ASYM_KEY_MODULUS_LENGTH = 4096;
	private static final BigInteger ASYM_KEY_EXPONENT = new BigInteger("17", 10);
	private static final int ASYM_KEY_CERTAINTY = 80;
	
	public static final String MAILSITE_SUFFIX = "mailsite";
	public static final String MAILSITE_VERSION = "-1";
	

	public static void Create(String username) throws IOException {
		File datadir = new File(DATADIR);
		if (!datadir.exists()) {
			if (!datadir.mkdir()) throw new IOException("Failed to create data directory");
		}
		
		File accountdir = new File(DATADIR, username);
		if (!accountdir.mkdir()) throw new IOException("Failed to create directory "+username+" in "+DATADIR);
		
		putWelcomeMessage(username, new EmailAddress(username+"@"+getFreemailDomain(accountdir)));
	}
	
	public static void setupNIM(String username) throws IOException {
		File accountdir = new File(DATADIR, username);
		
		File contacts_dir = new File(accountdir, SingleAccountWatcher.CONTACTS_DIR);
		if (!contacts_dir.exists()) {
			if (!contacts_dir.mkdir()) throw new IOException("Failed to create contacts directory");
		}
		
		File nimdir = new File(contacts_dir, NIMDIR);
		if (!nimdir.exists()) {
			if (!nimdir.mkdir()) throw new IOException("Failed to create nim directory");
		}
		
		File keyfile = new File(nimdir, NIMContact.KEYFILE);
		PrintWriter pw = new PrintWriter(new FileOutputStream(keyfile));
		
		pw.println(MessageSender.NIM_KEY_PREFIX + username + "-");
		
		pw.close();
	}
	
	public static void ChangePassword(String username, String newpassword) throws Exception {
		MD5Digest md5 = new MD5Digest();
		
		File accountdir = new File(DATADIR, username);
		if (!accountdir.exists()) {
			throw new Exception("No such account - "+username+".");
		}
		
		PropsFile accfile = getAccountFile(accountdir);
		
		md5.update(newpassword.getBytes(), 0, newpassword.getBytes().length);
		byte[] md5passwd = new byte[md5.getDigestSize()];
		md5.doFinal(md5passwd, 0);
		String strmd5 = new String(Hex.encode(md5passwd));
		
		accfile.put("md5passwd", strmd5);
	}
	
	public static PropsFile getAccountFile(File accdir) {
		PropsFile accfile = new PropsFile(new File(accdir, ACCOUNT_FILE));
		
		if (accdir.exists() && !accfile.exists()) {
			initAccFile(accfile);
		}
		
		return accfile;
	}
	
	public static String getFreemailDomain(File accdir) {
		PropsFile accfile = getAccountFile(accdir);
		
		return getFreemailDomain(accfile);
	}
	
	public static String getFreemailDomain(PropsFile accfile) {
		FreenetURI mailsite;
		try {
			mailsite = new FreenetURI(accfile.get("mailsite.pubkey"));
		} catch (MalformedURLException mfue) {
			System.out.println("Warning: Couldn't fetch mailsite public key from account file! Your account file is probably corrupt.");
			return null;
		}
		
		return Base32.encode(mailsite.getKeyBody().getBytes())+".freemail";
	}
	
	public static String getKSKFreemailDomain(File accdir) {
		PropsFile accfile = getAccountFile(accdir);
		
		String alias = accfile.get("domain_alias");
		
		if (alias == null) return null;
		
		return alias+".freemail";
	}
	
	public static RSAKeyParameters getPrivateKey(File accdir) {
		PropsFile props = getAccountFile(accdir);
		
		String mod_str = props.get("asymkey.modulus");
		String privexp_str = props.get("asymkey.privexponent");
		
		if (mod_str == null || privexp_str == null) {
			System.out.println("Couldn't get private key - account file corrupt?");
			return null;
		}
		
		return new RSAKeyParameters(true, new BigInteger(mod_str, 32), new BigInteger(privexp_str, 32));
	}
	
	private static void initAccFile(PropsFile accfile) {
		try {
			System.out.println("Generating mailsite keys...");
			HighLevelFCPClient fcpcli = new HighLevelFCPClient();
			
			SSKKeyPair keypair = null;
			try {
				 keypair = fcpcli.makeSSK();
			} catch (ConnectionTerminatedException cte) {
				// leave keypair as null 
			}
			
			if (keypair == null) {
				System.out.println("Unable to connect to the Freenet node");
				return;
			}
			
			// write private key
			if (!accfile.put("mailsite.privkey", keypair.privkey+MAILSITE_SUFFIX)) {
				throw new IOException("Unable to write account file");
			}
			
			// write public key
			if (!accfile.put("mailsite.pubkey", keypair.pubkey+MAILSITE_SUFFIX)) {
				throw new IOException("Unable to write account file");
			}
			
			// initialise RTS KSK
			Random rnd = new Random();
			String rtskey = new String();
			
			int i;
			for (i = 0; i < RTS_KEY_LENGTH; i++) {
				rtskey += (char)(rnd.nextInt(25) + (int)'a');
			}
			
			if (!accfile.put("rtskey", rtskey)) {
				throw new IOException("Unable to write account file");
			}
			
			System.out.println("Mailsite keys generated.");
			System.out.println("Your Freemail address is any username followed by '@"+getFreemailDomain(accfile)+"'");
		} catch (IOException ioe) {
			System.out.println("Couldn't create mailsite key file! "+ioe.getMessage());
		}
		
		// generate an RSA keypair
		System.out.println("Generating cryptographic keypair (this could take a few minutes)...");
		
		SecureRandom rand = new SecureRandom();

		RSAKeyGenerationParameters kparams = new RSAKeyGenerationParameters(ASYM_KEY_EXPONENT, rand, ASYM_KEY_MODULUS_LENGTH, ASYM_KEY_CERTAINTY);

		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
		kpg.init(kparams);
		
		AsymmetricCipherKeyPair keypair = kpg.generateKeyPair();
		RSAKeyParameters pub = (RSAKeyParameters) keypair.getPublic();
		RSAKeyParameters priv = (RSAKeyParameters) keypair.getPrivate();

		accfile.put("asymkey.modulus", pub.getModulus().toString(32));
		accfile.put("asymkey.pubexponent", pub.getExponent().toString(32));
		accfile.put("asymkey.privexponent", priv.getExponent().toString(32));
		
		System.out.println("Account creation completed.");
	}
	
	public static void addShortAddress(String username, String alias) throws Exception {
		File accountdir = new File(DATADIR, username);
		if (!accountdir.exists()) {
			throw new Exception("No such account - "+username+".");
		}
		
		PropsFile accfile = getAccountFile(accountdir);
		
		alias = alias.toLowerCase();
		
		MailSite ms = new MailSite(accfile);
		
		if (ms.insertAlias(alias)) {
			accfile.put("domain_alias", alias);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
			EmailAddress to = new EmailAddress(username+"@"+getKSKFreemailDomain(accountdir));
		
			MessageBank mb = new MessageBank(username);
		
			MailMessage m = mb.createMessage();
		
			m.addHeader("From", "Freemail Daemon <nowhere@dontreply>");
			m.addHeader("To", to.toString());
			m.addHeader("Subject", "Your New Address");
			m.addHeader("Date", sdf.format(new Date()));
			m.addHeader("Content-Type", "text/plain;charset=\"us-ascii\"");
			m.addHeader("Content-Transfer-Encoding", "7bit");
			m.addHeader("Content-Disposition", "inline");
		
			PrintStream ps = m.writeHeadersAndGetStream();
		
			ps.println("Hi!");
			ps.println("");
			ps.println("This is to inform you that your new short Freemail address is:");
			ps.println("");
			ps.println(to);
			ps.println("");
			ps.println("Your long Freemail address will continue to work. If you have had previous short addresses, you should not rely on them working any longer.");
			
		
			m.commit();
		}
	}
	
	public static boolean authenticate(String username, String password) {
		if (!validate_username(username)) return false;
		
		//String sep = System.getProperty("file.separator");
		
		File accountdir = new File(DATADIR, username);
		if (!accountdir.exists()) {
			return false;
		}
		PropsFile accfile = getAccountFile(accountdir);
		
		String realmd5str = accfile.get("md5passwd");
		if (realmd5str == null) return false;
		
		MD5Digest md5 = new MD5Digest();
		md5.update(password.getBytes(), 0, password.getBytes().length);
		byte[] givenmd5 = new byte[md5.getDigestSize()];
		md5.doFinal(givenmd5, 0);
		
		String givenmd5str = new String(Hex.encode(givenmd5));
		
		if (realmd5str.equals(givenmd5str)) {
			return true;
		}
		return false;
	}
	
	private static boolean validate_username(String username) {
		if (username.length() < 1) return false;
		if (username.matches("[\\w_]*")) return true;
		return false;
	}
	
	private static void putWelcomeMessage(String username, EmailAddress to) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		
		MessageBank mb = new MessageBank(username);
		
		MailMessage m = mb.createMessage();
		
		m.addHeader("From", "Dave Baker <dave@dbkr.freemail>");
		m.addHeader("To", to.toString());
		m.addHeader("Subject", "Welcome to Freemail!");
		m.addHeader("Date", sdf.format(new Date()));
		m.addHeader("Content-Type", "text/plain;charset=\"us-ascii\"");
		m.addHeader("Content-Transfer-Encoding", "7bit");
		m.addHeader("Content-Disposition", "inline");
		
		PrintStream ps = m.writeHeadersAndGetStream();
		
		ps.println("Welcome to Freemail!");
		ps.println("");
		ps.println("Thanks for downloading and testing Freemail. You can get started and send me a Freemail now by hitting 'reply'.");
		ps.println("Your new Freemail address is:");
		ps.println("");
		ps.println(to);
		ps.println("");
		ps.println("But you'll probably want a shorter one. To do this, run Freemail with the --shortaddress argument, followed by your account name and the part you'd like before the '.freemail'. For example:");
		ps.println("");
		ps.println("java -jar freemail.jar --shortaddress bob bobshouse");
		ps.println("");
		ps.println("Try to pick something unique - Freemail will tell you if somebody has already taken the address you want. These short addresses are *probably* secure, but not absolutely. If you want to be sure, use the long address.");
		ps.println("");
		ps.println("If you find a bug, or would like something changed in Freemail, visit our bug tracker at https://bugs.freenetproject.org/ (and select 'Freemail' in the top right). You can also drop into #freemail on irc.freenode.net to discuss, or sign up to the mailing list at http://emu.freenetproject.org/cgi-bin/mailman/listinfo/freemail.");
		ps.println("");
		ps.println("Happy Freemailing!");
		ps.println("");
		ps.println("");
		ps.println("");
		ps.println("");
		ps.println("Dave Baker");
		ps.println("(Freemail developer)");
		
		m.commit();
	}
}
