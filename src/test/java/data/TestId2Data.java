/*
 * TestId2Data.java
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

public class TestId2Data {
	private static final String BASE64_ID = "XrAUYiUcz6zW9rJq990bwanm7lME-PoK4FQcUX2VfpY";
	private static final String BASE32_ID;
	static {
		try {
			BASE32_ID = Base32.encode(Base64.decode(BASE64_ID));
		} catch (IllegalBase64Exception e) {
			throw new AssertionError(e);
		}
	}

	private static final String REQUEST_KEY = "USK@" + BASE64_ID + ",QQioyJxvCSNB4ZhSsbJIbIvz5XAHSY8rSrv~OEPxiIo,AQACAAE";
	private static final String INSERT_KEY = "USK@UI3UuvXXx~T0nNh0NM6v6vO7Eb919bpnjlaEdwBZ-tU,QQioyJxvCSNB4ZhSsbJIbIvz5XAHSY8rSrv~OEPxiIo,AQECAAE";

	private static final String NICKNAME = "testid2";
	private static final String RTS_BASE = "qxdskdyuzeclgfrmxcyqkuujtangegit";

	private static final String MODULUS = "1hiqd4mrh535joblsdl4amhqevrmc968eq9knin9hiqn0holnddfhlpno44tc81pcfhjngn88cr4v2oaffr1f1fkpc440gs68gpb61d5rtfom2p01v1ckdmo4ohsepj7ae2e9rjdpbuia6onuflohrl2278ho0051mo3ul3s0aeqerbdd1lnu05r8qdd7ghc4v2dh6vvngpid56qv2c05e0flgqvhi3aa1erjtrgh4vh7cvn0bv1hhnucdrcdro8g1ltshnbmf73vlpqr3atp5d8a83lotiu9rlv918k2pm2vkqbkbjl64v0g985tbhb6pfhubibdjcthevi9f6u4kn8n0e58vmgk2ckrm3a0vg4au9f8hciboorljrs7l0uq6pd3l5sfpd824nq5r6kq24714a9t8e78bltocsokshhq9omqglifbf09rdlvm31ipc0kpoe80v09q8quo5e8d2qcdob9gr043sb8cn8sg56575oupglec09vh53kgb1pvtj1l9b70cvccrp1j4ktiqc0jbpvs8qck84i284s08vurfu6v3dnnl12hh79ta91p9rhnlj9no830qkl2e5f7aaqnllemt0najgmhs304ei96724jtduvt4juk62tq5cg0ctjth31u085r2mr38vdm1ius9b8o0nh0v6pio9e1i1ir8skp3jdrbp8uarvqm4ba2vb6idt25t2oh47rj7tj0n7pkb0p4b8anha7rhs9nh231mnavsj5eb66fct56r3jrljf3t50dfgnkd68po08rq7avcorv0p4f4sml6kt75hj0gost";
	private static final String PUBEXPONENT = "h";
	private static final String PRIVEXPONENT = "5qltp0pqfl2hc8stma0ga5rimia56nad16p0shpj0alh06lc5avua30t64avhse8vu9r7jllm6umfc8od521okl8k80f3rp6kpi87m871ipfopgub5pukdv2e5rq6or6s096pqo6nvqk0pfvqa2sehh5t2se3ofkt0uil9rog98oantq06ab1sthvavliscn5h77lgu2oi25a0pobiba1kodqef05sk15q70epqd9vu8cqa7ib5qh4mco6viiac9dbvo22nus4j9bv8porke85acg0drcsijvfocbe6430mu2ecl70dp67d7i0uq1befrf07aontqo3f7d413lgn4a8hogn6mhml5tn54febd3ovcv7e5puclnk69ro0sqetdv762fo1renh66i4efqtddlh2ar6caekp9uglb4qj644e61fma6vscrded41un677m8pg66soetetik8dvr55dtu7d8uult9kg71m4e28d64jd0rpv964468guaminbqadg7u8cgom35tooj5q50vpmkalpthj33eje1nqk8gjec735qav70cf5eifii3u1hqbocn1hgq5qviafm0jte67qcfeei0f9da4fmgcfth5pjqjbkm5bag1en2hvr0chprbn9eqdqsnr5vjdddptptcaogr2lod1hp6fliob889kks53q361jrc19f6u9f6dvcuf32fggop13sc15gbfos5d03j4nna76f28sduhufa4cb754te4bm6qhgiq2bber9fpmcn1bktdvn5t11orjjmpgp9scv99m2tbmeohgoig6t1bnsd9";


	//FIXME: This will almost certainly break at some point for tests running around midnight UTC
	public static final String RTSKEY = "KSK@" + RTS_BASE + "-" + DateStringFactory.getKeyString();

	public static class Identity {
		public static final String ID = BASE64_ID;
		public static final String REQUEST_URI = REQUEST_KEY + "/WebOfTrust/0";

		@SuppressWarnings("hiding")
		public static final String NICKNAME = TestId2Data.NICKNAME;

		public static org.freenetproject.freemail.wot.Identity newInstance() {
			return new org.freenetproject.freemail.wot.Identity(ID, REQUEST_URI, NICKNAME);
		}
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
		public static final String RTSKEY = "rtsksk=" + TestId2Data.RTS_BASE;

		public static final String ASYMKEY_MODULUS = "asymkey.modulus=" + MODULUS;
		public static final String ASYMKEY_PUBEXPONENT = "asymkey.pubexponent=" + PUBEXPONENT;

		public static final String CONTENT =
				RTSKEY + "\n"
				+ ASYMKEY_MODULUS + "\n"
				+ ASYMKEY_PUBEXPONENT + "\n";

		@SuppressWarnings("hiding")
		public static final String REQUEST_KEY = TestId2Data.REQUEST_KEY + "/mailsite/-1/mailpage";
		@SuppressWarnings("hiding")
		public static final String INSERT_KEY = TestId2Data.INSERT_KEY + "/mailsite/-1/mailpage";

		public static final int EDITION = 1;
	}
}
