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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import freemail.utils.Logger;

public class AccountManager {
	// this really doesn't matter a great deal
	public static final String NIMDIR = "nim";
	
	private static final String ACCOUNT_FILE = "accprops";
	private static final int RTS_KEY_LENGTH = 32;
	
	private static final int ASYM_KEY_MODULUS_LENGTH = 4096;
	private static final BigInteger ASYM_KEY_EXPONENT = new BigInteger("17", 10);
	private static final int ASYM_KEY_CERTAINTY = 80;
	
	public static final String MAILSITE_SUFFIX = "mailsite";
	public static final String MAILSITE_VERSION = "-1";
	
	// We keep FreemailAccount objects for all the accounts in this instance of Freemail - they need to be in memory
	// anyway since there's SingleAccountWatcher thread running for each of them anyway - and we return the same object
	// each time a request is made for a given account.
	private Map/*<String, FreemailAccount>*/ accounts = new HashMap();
	
	private final File datadir;
	
	public AccountManager(File _datadir) {
		datadir = _datadir;
		if (!datadir.exists()) {
			datadir.mkdir();
		}
		
		File[] files = datadir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;

			String invalid=validateUsername(files[i].getName());
			if(!invalid.equals("")) {
				Logger.error(this,"Account name "+files[i].getName()+" contains invalid chars (\""+invalid
						+"\"), you may get problems accessing the account.");
			}
			
			FreemailAccount account = new FreemailAccount(files[i].getName(), files[i], getAccountFile(files[i]));
			if (account == null) {
				Logger.error(this, "Couldn't initialise account from directory '"+files[i].getName()+"' - ignoring.");
			}
			
			accounts.put(files[i].getName(), account);
		}
	}
	
	public FreemailAccount getAccount(String username) {
		return (FreemailAccount)accounts.get(username);
	}
	
	public List/*<FreemailAccount>*/ getAllAccounts() {
		return new LinkedList(accounts.values());
	}

	// avoid invalid chars in username or address
	// returns the first invalid char to give user a hint
	private static String validateChars(String username, String invalid) {
		for(int i=0;i<invalid.length();i++) {
			if(username.indexOf(invalid.substring(i,i+1))>=0) {
				return invalid.substring(i,i+1);
			}
		}
		return "";
	}

	// @ plus chars that may be invalid as filenames
	public static String validateUsername(String username) {
		return validateChars(username, "@\'\"\\/ :");
	}

	// @, space and other email meta chars
	public static String validateShortAddress(String username) {
		return validateChars(username, "@\'\"\\/,:%()+ ");
	}
	
	public FreemailAccount createAccount(String username) throws IOException,IllegalArgumentException {
		String invalid=validateUsername(username);
		if(!invalid.equals("")) {
			throw new IllegalArgumentException("The username may not contain the character '"+invalid+"'");
		}
		
		File accountdir = new File(datadir, username);
		if (!accountdir.exists() && !accountdir.mkdir()) throw new IOException("Failed to create directory "+username+" in "+datadir);
		
		PropsFile accProps = newAccountFile(accountdir);
		
		FreemailAccount account = new FreemailAccount(username, accountdir, accProps);
		accounts.put(username, account);
		
		putWelcomeMessage(account, new EmailAddress(username+"@"+getFreemailDomain(accProps)));
		
		return account;
	}
	
	public void setupNIM(String username) throws IOException {
		File accountdir = new File(datadir, username);
		
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
	
	public static void changePassword(FreemailAccount account, String newpassword) throws Exception {
		MD5Digest md5 = new MD5Digest();
		
		md5.update(newpassword.getBytes(), 0, newpassword.getBytes().length);
		byte[] md5passwd = new byte[md5.getDigestSize()];
		md5.doFinal(md5passwd, 0);
		String strmd5 = new String(Hex.encode(md5passwd));
		
		account.getProps().put("md5passwd", strmd5);
	}
	
	private static PropsFile getAccountFile(File accdir) {
		PropsFile accfile = new PropsFile(new File(accdir, ACCOUNT_FILE));
		
		if (!accdir.exists() || !accfile.exists()) {
			return null;
		}
		
		return accfile;
	}
	
	private static PropsFile newAccountFile(File accdir) {
		PropsFile accfile = new PropsFile(new File(accdir, ACCOUNT_FILE));
		
		if (accdir.exists() && !accfile.exists()) {
			initAccFile(accfile);
		}
		
		return accfile;
	}
	
	public static String getFreemailDomain(PropsFile accfile) {
		FreenetURI mailsite;
		try {
			String pubkey=accfile.get("mailsite.pubkey");
			if(pubkey==null) {
				return null;
			}
			mailsite = new FreenetURI(pubkey);
		} catch (MalformedURLException mfue) {
			Logger.error(AccountManager.class,"Warning: Couldn't fetch mailsite public key from account file! Your account file is probably corrupt.");
			return null;
		}
		
		return Base32.encode(mailsite.getKeyBody().getBytes())+".freemail";
	}
	
	public static String getKSKFreemailDomain(PropsFile accfile) {
		String alias = accfile.get("domain_alias");
		
		if (alias == null) return null;
		
		return alias+".freemail";
	}
	
	public static RSAKeyParameters getPrivateKey(PropsFile props) {
		String mod_str = props.get("asymkey.modulus");
		String privexp_str = props.get("asymkey.privexponent");
		
		if (mod_str == null || privexp_str == null) {
			Logger.error(AccountManager.class,"Couldn't get private key - account file corrupt?");
			return null;
		}
		
		return new RSAKeyParameters(true, new BigInteger(mod_str, 32), new BigInteger(privexp_str, 32));
	}
	
	private static void initAccFile(PropsFile accfile) {
		try {
			Logger.normal(AccountManager.class,"Generating mailsite keys...");
			HighLevelFCPClient fcpcli = new HighLevelFCPClient();
			
			SSKKeyPair keypair = null;
			try {
				 keypair = fcpcli.makeSSK();
			} catch (ConnectionTerminatedException cte) {
				// leave keypair as null 
			}
			
			if (keypair == null) {
				Logger.normal(AccountManager.class,"Unable to connect to the Freenet node");
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
			
			Logger.normal(AccountManager.class,"Mailsite keys generated.");
			Logger.normal(AccountManager.class,"Your Freemail address is any username followed by '@"+getFreemailDomain(accfile)+"'");
		} catch (IOException ioe) {
			Logger.error(AccountManager.class,"Couldn't create mailsite key file! "+ioe.getMessage());
		}
		
		// generate an RSA keypair
		Logger.normal(AccountManager.class,"Generating cryptographic keypair (this could take a few minutes)...");
		
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
		
		Logger.normal(AccountManager.class,"Account creation completed.");
	}
	
	public static boolean addShortAddress(FreemailAccount account, String alias) throws Exception {
		String invalid=validateShortAddress(alias);
		if(!invalid.equals("")) {
			throw new IllegalArgumentException("The short address may not contain the character '"+invalid+"'");
		}
		
		alias = alias.toLowerCase();
		
		MailSite ms = new MailSite(account.getProps());
		
		if (ms.insertAlias(alias)) {
			account.getProps().put("domain_alias", alias);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
			EmailAddress to = new EmailAddress(account.getUsername()+"@"+getKSKFreemailDomain(account.getProps()));
		
			MailMessage m = account.getMessageBank().createMessage();
		
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
			return true;
		} else {
			return false;
		}
	}
	
	public FreemailAccount authenticate(String username, String password) {
		if (!validate_username(username)) return null;
		
		FreemailAccount account = (FreemailAccount)accounts.get(username);
		if (account == null) return null;
		
		String realmd5str = account.getProps().get("md5passwd");
		if (realmd5str == null) return null;
		
		MD5Digest md5 = new MD5Digest();
		md5.update(password.getBytes(), 0, password.getBytes().length);
		byte[] givenmd5 = new byte[md5.getDigestSize()];
		md5.doFinal(givenmd5, 0);
		
		String givenmd5str = new String(Hex.encode(givenmd5));
		
		if (realmd5str.equals(givenmd5str)) {
			return account;
		}
		return null;
	}
	
	private static boolean validate_username(String username) {
		if (username.length() < 1) return false;
		if (username.matches("[\\w_]*")) return true;
		return false;
	}
	
	private static void putWelcomeMessage(FreemailAccount account, EmailAddress to) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		
		MailMessage m = account.getMessageBank().createMessage();
		
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
		ps.println("There are several places to discuss Freemail, report problems, or ask for help:");
		ps.println(" * The 'freemail' board on FMS");
		ps.println(" * The bug tracker: https://bugs.freenetproject.org/ (select 'Freemail' in the top right).");
		ps.println(" * #freemail on irc.freenode.net");
		ps.println(" * The mailing list at http://emu.freenetproject.org/cgi-bin/mailman/listinfo/freemail.");
		ps.println("");
		ps.println("Don't forget to stay up to date with the Freemail news and latest version at the freesite, which can be found at:");
		ps.println("");
		ps.println("USK@xOg49GNltumTJJzj0fVzuGDpo4hJUsy2UsGQkjE7NY4,EtUH5b9gGpp8JiY-Bm-Y9kHX1q-yDjD-9oRzXn21O9k,AQACAAE/freemail/-1/");
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
