/*
 * TestId1Data.java
 * This file is part of Freemail
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

package data;

import org.archive.util.Base32;
import org.freenetproject.freemail.utils.DateStringFactory;

import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class TestId1Data {
	private static final String BASE64_ID = "vUwUGaBlXbMOWdhSr5BlrxkZfD7bUGOUk6M0HNYKN2A";
	private static final String BASE32_ID;
	static {
		try {
			BASE32_ID = Base32.encode(Base64.decode(BASE64_ID));
		} catch (IllegalBase64Exception e) {
			throw new AssertionError(e);
		}
	}

	private static final String REQUEST_KEY = "USK@" + BASE64_ID + ",mMhITeChRejfV~jEqKNLzcKsTl6LpmPcT8e4GSxfEDI,AQACAAE";
	private static final String INSERT_KEY = "USK@C1N6qfKSm0t5W1C9UoYI1REoPoA2vFmEvs3KcJYykkE,mMhITeChRejfV~jEqKNLzcKsTl6LpmPcT8e4GSxfEDI,AQECAAE";

	private static final String NICKNAME = "testid1";
	private static final String RTS_BASE = "wuchpeusibcrbprnugngqgjtfymmscsd";

	private static final String MODULUS = "14k5kbn3tklm47m50e6nao24noqk31am67jr8thjcmvd9tkggtfndfscs91ti93lisr91n0hjs3v3bale3cljsampslv364gj0h7gfbanchl1d5brrkp434qt44ipj3f09v0vig9sks2tkub0pe14hnmga8da507319l9c3dma71jikvti8974g4tugkerq654garv6trfp03phvt1j7vmr9sc4db049qfv1fe5fl4e3qcmclpdobg35r4jp9vtlmh6s5636q7hftt10ls2s910v6ag5f7sgr5tttljfaej8mbhh7i9p6mlbdrapvjg9pr7iqnorgbkhe2oju5uhch91q78djs8d3kcjvdpc9lbjai8718uf3c9532hp7b58clverdr2l0eu0jqo9jrahhvt61s6jkb92k5538fjncdtj5shtems8ps3pmcuo4lc3vsi7n765thtkoa97i4v806s491dlsir9hvhtmg1se8835l20rjitqjp3od2vt4rd19v57o3ep4g82qf8fl638tetgmg1jdhmncm4hnb7j022ojeovltamjh3ub9qjrh31heem6273iesmqh4v50imloivl6dd5m6gcj5j8r1i404e8pl4349lslc38ta514p03lp4ive01pndi57t03477r6f8m8f83hl1i74l765kt3d4n1buocde18jcbmgui1sgr17k34hkpdf4lued2g61t8g0mcr1bqa8n6iiec8434f4in2jgtpo7kg6f0f773go9f3391egg0p6vsb7d9h2hghrpmgmuqdm89ja1ns01h9n3kn5l";
	private static final String PUBEXPONENT = "h";
	private static final String PRIVEXPONENT = "24uebvf98nrkui7qc454cmmro1i3u68qn15bq6lr37fqtvac98ehngnb2d98jbgago65nu4pkf9bfn6t22k31lao3i7f7sg13pfg0ssrnaed89n95cgh5qkd07pie9jmjd3qag0ik1ks33lbat1ucappdousqkn05n50hkt1a0d8lpmiloul40ud1pec5cqmsg0kf0d1jo1seloesahd53fkfonbu40ifvu6hopch6f92k4gat1dr9j5bi3bs7ci8hb26061hcab5f3po7mbqd7fdpdfuijq27d1nrfune9thjrbnl90ck2ho691rg0ieotavggllcg2mo15h9adt81vl48aija5rhls6eldnr14cbos9u0sdsookrcosshdtgt3fru8ofu015aqtcsrcb97saalv4prvshj316t7783de8cngb1o0ddusn1ps12up5ten4qedbpq4f9fffnq8555743hkpcoe3ktks2rgv7mpkrmbnkc0iknhut2ffamf7aenkcb16esraadu96fe63h7hvpb7mqc6mej2qsc50mgjd2ci8lhnc3nfb4ghv1somn535ip7pv5f55fn19h1a44lgsejgle9485t8pv52v3og6u5k2mfv635is2n9gc2f8e758se2iaeatd6aq2f6uqabvf3k7ige6510pdih7n34b8m3i7alh1velorvik3a578homh4j86a8gvl5stecuspbanp5bdrpf7nhe5rg0nqvboilbjqdouin1nibgu0bkrgs288o89frn8nlgpf8jni87i8ta4i6mv7odtqbjuiv2t";


	//FIXME: This will almost certainly break at some point for tests running around midnight UTC
	public static final String RTSKEY = "KSK@" + RTS_BASE + "-" + DateStringFactory.getKeyString();

	public static class Identity {
		public static final String ID = BASE64_ID;
		public static final String REQUEST_URI = REQUEST_KEY + "/WebOfTrust/0";

		@SuppressWarnings("hiding")
		public static final String NICKNAME = TestId1Data.NICKNAME;
	}

	public static class FreemailAccount {
		public static final String ADDRESS = NICKNAME + "@" + BASE32_ID + ".freemail";
		public static final String ADDRESS_WITH_ANGLE = NICKNAME + "<" + ADDRESS + ">";

		public static final String IDENTITY = BASE64_ID;

		public static final String ACCPROPS_ASYMKEY_MODULUS = "asymkey.modulus=" + MODULUS;
		public static final String ACCPROPS_NICKNAME = "nickname=" + NICKNAME;
		public static final String ACCPROPS_RTSKEY = "rtskey=" + RTS_BASE;
		public static final String ACCPROPS_ASYMKEY_PUBEXPONENT = "asymkey.pubexponent=" + PUBEXPONENT;
		public static final String ACCPROPS_MAILSITE_PRIVKEY = "mailsite.privkey=" + INSERT_KEY + "/mailsite/";
		public static final String ACCPROPS_ASYMKEY_PRIVEXPONENT = "asymkey.privexponent=" + PRIVEXPONENT;

		public static final String ACCPROPS_CONTENT =
				ACCPROPS_ASYMKEY_MODULUS + "\n"
				+ ACCPROPS_NICKNAME + "\n"
				+ ACCPROPS_RTSKEY + "\n"
				+ ACCPROPS_ASYMKEY_PUBEXPONENT + "\n"
				+ ACCPROPS_MAILSITE_PRIVKEY + "\n"
				+ ACCPROPS_ASYMKEY_PRIVEXPONENT + "\n";
	}

	public static class Mailsite {
		@SuppressWarnings("hiding")
		public static final String RTSKEY = "rtsksk=" + TestId1Data.RTS_BASE;

		public static final String ASYMKEY_MODULUS = "asymkey.modulus=" + MODULUS;
		public static final String ASYMKEY_PUBEXPONENT = "asymkey.pubexponent=" + PUBEXPONENT;

		public static final String CONTENT =
				RTSKEY + "\n"
				+ ASYMKEY_MODULUS + "\n"
				+ ASYMKEY_PUBEXPONENT + "\n";

		@SuppressWarnings("hiding")
		public static final String REQUEST_KEY = TestId1Data.REQUEST_KEY + "/mailsite/-1/mailpage";
		@SuppressWarnings("hiding")
		public static final String INSERT_KEY = TestId1Data.INSERT_KEY + "/mailsite/-1/mailpage";

		public static final int EDITION = 1;
	}
}
